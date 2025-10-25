package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.dto.*;
import com.bidwave.onlineauctionhub.models.*;
import com.bidwave.onlineauctionhub.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final ReportService reportService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService,
                       VerificationTokenRepository tokenRepository, AuctionRepository auctionRepository,
                       BidRepository bidRepository, NotificationService notificationService,
                       NotificationRepository notificationRepository,
                       ReportService reportService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.reportService = reportService;
    }

    public User registerUser(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("Email already in use");
        }

        // Create the appropriate user type based on the role
        User user;
        if ("SELLER".equalsIgnoreCase(request.role())) {
            user = new Seller();
        } else {
            // Default to Buyer if role is not Seller or is null
            user = new Buyer();
        }

        // Set the common user details
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRegistrationDate(LocalDateTime.now());
        user.setStatus("ACTIVE");

        // Save the user with enabled=false first
        user.setEnabled(false);
        User savedUser = userRepository.save(user);

        // Generate token using UUID system
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(savedUser.getEmail(), token);

        // Broadcast real-time dashboard update after new user registration
        reportService.broadcastDashboardUpdate();

        return savedUser;
    }

    public void verifyUser(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid verification token."));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Verification token has expired.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);

        // Notify user about successful verification
        notificationService.createNotification(user, "Your account has been verified and activated!");
    }

    public UserProfileDto getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return UserProfileDto.fromEntity(currentUser);
    }

    public UserProfileDto updateUserProfile(UpdateProfileRequest updateRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        currentUser.setFirstName(updateRequest.firstName());
        currentUser.setLastName(updateRequest.lastName());

        User updatedUser = userRepository.save(currentUser);

        // Notify user about profile update
        notificationService.createNotification(updatedUser, "Your profile has been updated successfully.");

        return UserProfileDto.fromEntity(updatedUser);
    }

    // Directly updates the user's profile
    public User updateProfile(String userEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User updatedUser = userRepository.save(user);

        // Notify user about profile update
        notificationService.createNotification(updatedUser, "Your profile has been updated successfully.");

        return updatedUser;
    }

    public void changeUserPassword(ChangePasswordRequest passwordRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(passwordRequest.currentPassword(), currentUser.getPassword())) {
            throw new IllegalStateException("Incorrect current password");
        }

        if (passwordRequest.newPassword().length() < 8) {
            throw new IllegalStateException("Password must be at least 8 characters");
        }

        currentUser.setPassword(passwordEncoder.encode(passwordRequest.newPassword()));
        userRepository.save(currentUser);

        // Notify user about password change
        notificationService.createNotification(currentUser, "Your password has been changed successfully.");
    }

    public List<UserProfileDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserProfileDto::fromEntity)
                .collect(Collectors.toList());
    }

    public UserProfileDto updateUserStatus(Long userId, UpdateUserStatusRequest statusRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        user.setStatus(statusRequest.status());
        User updatedUser = userRepository.save(user);

        // Notify user about status change (e.g., suspended/active)
        notificationService.createNotification(updatedUser, "Your account status has been updated to: " + statusRequest.status());

        return UserProfileDto.fromEntity(updatedUser);
    }

    /**
     * Admin-facing method to delete a user.
     * Calls the shared deleteUserAccount logic.
     * @param userId The ID of the user to delete.
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        // Call the shared deletion logic
        deleteUserAccount(user);
    }

    /**
     * User-facing method for a user to delete their own profile.
     * Calls the shared deleteUserAccount logic.
     * @param userEmail The email of the authenticated user.
     */
    public void deleteProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Call the shared deletion logic
        deleteUserAccount(user);
    }

    /**
     * Private helper method to centrally handle user deletion logic.
     */
    private void deleteUserAccount(User user) {
        // 1. Business logic checks
        if (user instanceof Seller) {
            if (auctionRepository.countBySeller((Seller) user) > 0) {
                throw new IllegalStateException("Cannot delete seller: This user has auctions. Please suspend the account instead.");
            }
        }
        if (user instanceof Buyer) {
            if (bidRepository.countByBuyer((Buyer) user) > 0) {
                throw new IllegalStateException("Cannot delete buyer: This user has placed bids. Please suspend the account instead.");
            }
        }

        // 2. Clean up child records
        notificationRepository.deleteAllByUser(user);
        tokenRepository.deleteByUser(user);

        // 3. Delete the user
        userRepository.delete(user);

        // Broadcast real-time dashboard update after user deletion
        reportService.broadcastDashboardUpdate();
    }
}
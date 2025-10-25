package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.models.*;
import com.bidwave.onlineauctionhub.repositories.AnnouncementRepository;
import com.bidwave.onlineauctionhub.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public AnnouncementService(AnnouncementRepository announcementRepository,
                               UserRepository userRepository,
                               NotificationService notificationService) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Creates a new announcement, saves it, and sends notifications to the target audience.
     */
    public void createAnnouncement(String content, TargetAudience targetAudience, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found"));

        Announcement announcement = new Announcement(content, targetAudience, admin);
        announcementRepository.save(announcement);

        sendNotificationsFor(announcement);
    }

    /**
     * Fetches all announcements, newest first.
     */
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Helper method to send in-app notifications to the correct users.
     */
    private void sendNotificationsFor(Announcement announcement) {
        List<User> targetUsers = new ArrayList<>();
        String message = "New Admin Announcement: " + announcement.getContent().substring(0, Math.min(announcement.getContent().length(), 50)) + "...";

        // Get the admin who created the announcement
        User sendingAdmin = announcement.getCreatedBy();

        switch (announcement.getTargetAudience()) {
            case ALL:
                targetUsers = userRepository.findAll();
                break;
            case BUYERS:
                targetUsers = userRepository.findAllByUserRole(Buyer.class);
                break;
            case SELLERS:
                targetUsers = userRepository.findAllByUserRole(Seller.class);
                break;
        }

        // --- THE FIX: Filter out the sending admin from the recipient list ---
        List<User> finalRecipients = targetUsers.stream()
                .filter(user -> !user.getUserId().equals(sendingAdmin.getUserId()))
                .collect(Collectors.toList());
        // --- END OF FIX ---

        // Now, loop through the corrected list of recipients.
        for (User user : finalRecipients) {
            notificationService.createNotification(user, message);
        }
    }
}
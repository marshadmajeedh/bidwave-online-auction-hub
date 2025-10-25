package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.dto.*;
import com.bidwave.onlineauctionhub.models.*;
import com.bidwave.onlineauctionhub.repositories.*;
import com.bidwave.onlineauctionhub.service.finalization.FinalizationStrategy;
import com.bidwave.onlineauctionhub.service.validation.AuctionValidationStrategy;
import jakarta.persistence.criteria.Join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuctionService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BidRepository bidRepository;
    private final EmailService emailService;
    private final AdminRepository adminRepository;
    private final BuyerRepository buyerRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;
    private final AuctionValidationStrategy validationStrategy;
    private final List<FinalizationStrategy> finalizationStrategies;
    private final SimpMessagingTemplate messagingTemplate;
    private final ReportService reportService;

    @Autowired
    public AuctionService(
            AuctionRepository auctionRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            BidRepository bidRepository,
            EmailService emailService,
            AdminRepository adminRepository,
            BuyerRepository buyerRepository,
            NotificationService notificationService,
            FileStorageService fileStorageService,
            AuctionValidationStrategy validationStrategy,
            List<FinalizationStrategy> finalizationStrategies,
            SimpMessagingTemplate messagingTemplate,
            ReportService reportService) {
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.bidRepository = bidRepository;
        this.emailService = emailService;
        this.adminRepository = adminRepository;
        this.buyerRepository = buyerRepository;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
        this.validationStrategy = validationStrategy;
        this.finalizationStrategies = finalizationStrategies;
        this.messagingTemplate = messagingTemplate;
        this.reportService = reportService;
    }

    public Auction createAuction(AuctionCreateRequest request, MultipartFile[] imageFiles, String sellerEmail) {
        validationStrategy.validate(request, imageFiles);

        Seller seller = (Seller) userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found"));

        List<Category> foundCategories = categoryRepository.findAllById(request.categoryIds());
        if (foundCategories.isEmpty()) {
            throw new IllegalArgumentException("You must select at least one valid category.");
        }

        Auction auction = new Auction();
        auction.setItemName(request.itemName());
        auction.setDescription(request.description());
        auction.setStartPrice(request.startPrice());
        auction.setEndTime(request.endTime());
        auction.setSeller(seller);
        auction.setCategories(new HashSet<>(foundCategories));
        auction.setStatus("PENDING");

        try {
            if (imageFiles != null) {
                for (MultipartFile file : imageFiles) {
                    if (file != null && !file.isEmpty()) {
                        String imageUrl = fileStorageService.uploadFile(file);
                        auction.addImage(imageUrl);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not store image files. Error: " + e.getMessage());
        }

        Auction savedAuction = auctionRepository.save(auction);

        // Notify admins for approval
        List<Admin> admins = adminRepository.findAll();
        for (Admin admin : admins) {
            String message = "New auction '" + savedAuction.getItemName() + "' is pending your approval.";
            notificationService.createNotification(admin, message);
            emailService.sendAdminApprovalNotification(admin.getEmail(), savedAuction);
        }

        // Broadcast real-time dashboard update after new auction creation
        reportService.broadcastDashboardUpdate();

        return savedAuction;
    }

    public List<AuctionSummaryDto> getActiveAuctions(String searchTerm, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sort) {
        Specification<Auction> spec = (root, query, cb) -> cb.equal(root.get("status"), "ACTIVE");

        if (searchTerm != null && !searchTerm.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("itemName"), "%" + searchTerm + "%"),
                            cb.like(root.get("description"), "%" + searchTerm + "%")
                    )
            );
        }
        if (categoryId != null && categoryId > 0) {
            spec = spec.and((root, query, cb) -> {
                Join<Auction, Category> categoryJoin = root.join("categories");
                return cb.equal(categoryJoin.get("categoryId"), categoryId);
            });
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startPrice"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startPrice"), maxPrice));
        }

        Sort sortOrder = Sort.by("endTime").ascending();
        if ("newly_listed".equals(sort)) sortOrder = Sort.by("auctionId").descending();
        else if ("price_asc".equals(sort)) sortOrder = Sort.by("startPrice").ascending();
        else if ("price_desc".equals(sort)) sortOrder = Sort.by("startPrice").descending();

        List<Auction> auctions = auctionRepository.findAll(spec, sortOrder);
        return auctions.stream().map(AuctionSummaryDto::fromEntity).collect(Collectors.toList());
    }

    public List<AuctionSummaryDto> getAuctionsForSeller(String sellerEmail) {
        Seller seller = (Seller) userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found"));
        return auctionRepository.findAllBySeller(seller).stream()
                .map(AuctionSummaryDto::fromEntity)
                .collect(Collectors.toList());
    }

    public AuctionDetailsDto getAuctionDetails(Long id) {
        // Use findById instead of findByIdWithImages if the custom method doesn't exist
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found with ID: " + id));
        return AuctionDetailsDto.fromEntity(auction);
    }

    public SellerAuctionDetailDto getSellerAuctionDetails(Long auctionId, String sellerEmail) {
        // Use findById instead of findByIdWithImages if the custom method doesn't exist
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        if (!auction.getSeller().getEmail().equals(sellerEmail)) {
            throw new AccessDeniedException("You are not the owner of this auction.");
        }
        List<Bid> bids = bidRepository.findAllByAuctionOrderByBidAmountDesc(auction);
        return SellerAuctionDetailDto.fromEntity(auction, bids);
    }

    /**
     * Gets all auctions for the admin view without any filtering.
     * This is an overloaded method to fix the controller error.
     * @return A list of all auctions for the admin view.
     */
    public List<AdminAuctionViewDto> getAllAuctionsForAdmin() {
        return getAllAuctionsForAdmin(null, null);
    }

    public List<AdminAuctionViewDto> getAllAuctionsForAdmin(String sellerName, String status) {
        Specification<Auction> spec = (root, query, cb) -> cb.conjunction();

        if (sellerName != null && !sellerName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.join("seller").get("firstName"), "%" + sellerName + "%"));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        List<Auction> auctions = auctionRepository.findAll(spec, Sort.by("endTime").descending());
        return auctions.stream().map(AdminAuctionViewDto::fromEntity).collect(Collectors.toList());
    }

    public AuctionDetailsDto updateAuctionStatus(Long auctionId, UpdateAuctionStatusRequest statusRequest) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found with ID: " + auctionId));

        String newStatus = statusRequest.status();
        auction.setStatus(newStatus);

        if ("ACTIVE".equalsIgnoreCase(newStatus)) {
            notificationService.createNotification(auction.getSeller(),
                    "Your auction '" + auction.getItemName() + "' has been approved!");
            emailService.sendSellerAuctionApprovedNotification(auction.getSeller().getEmail(), auction);

            // Consider changing this to a more scalable notification strategy
            List<Buyer> buyers = buyerRepository.findAll();
            for (Buyer buyer : buyers) {
                notificationService.createNotification(buyer, "A new item is up for auction: " + auction.getItemName());
                emailService.sendNewAuctionNotificationToBuyer(buyer.getEmail(), auction);
            }
        } else if ("DISAPPROVED".equalsIgnoreCase(newStatus)) {
            notificationService.createNotification(auction.getSeller(),
                    "Your auction '" + auction.getItemName() + "' was disapproved.");
            emailService.sendSellerAuctionDisapprovedNotification(auction.getSeller().getEmail(), auction);
        }

        Auction updatedAuction = auctionRepository.save(auction);

        // Broadcast status update via WebSocket
        broadcastAuctionStatusUpdate(updatedAuction);

        // Broadcast real-time dashboard update after auction status change
        reportService.broadcastDashboardUpdate();

        return AuctionDetailsDto.fromEntity(updatedAuction);
    }

    public void deleteAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found with ID: " + auctionId));
        if (bidRepository.countByAuction(auction) > 0) {
            throw new IllegalStateException("Cannot delete auction: Bids have already been placed.");
        }
        auctionRepository.deleteById(auctionId);

        // Broadcast real-time dashboard update after auction deletion
        reportService.broadcastDashboardUpdate();
    }

    public void deleteSellerAuction(Long auctionId, String sellerEmail) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        if (!auction.getSeller().getEmail().equals(sellerEmail)) {
            throw new AccessDeniedException("You are not the owner of this auction.");
        }
        if (bidRepository.countByAuction(auction) > 0) {
            throw new IllegalStateException("Cannot delete auction: Bids have already been placed.");
        }
        auctionRepository.delete(auction);

        // Broadcast real-time dashboard update after auction deletion
        reportService.broadcastDashboardUpdate();
    }

    public Auction updateSellerAuction(Long auctionId, AuctionUpdateRequest request, String sellerEmail) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        if (!auction.getSeller().getEmail().equals(sellerEmail)) {
            throw new AccessDeniedException("You are not the owner of this auction.");
        }
        if (bidRepository.countByAuction(auction) > 0) {
            throw new IllegalStateException("Cannot edit auction: Bids have already been placed.");
        }

        auction.setItemName(request.itemName());
        auction.setDescription(request.description());
        auction.setStartPrice(request.startPrice());
        auction.setEndTime(request.endTime());
        auction.setCategories(new HashSet<>(categoryRepository.findAllById(request.categoryIds())));

        return auctionRepository.save(auction);
    }

    @Scheduled(fixedRate = 60000)
    public void finalizeExpiredAuctions() {
        List<Auction> expiredAuctions =
                auctionRepository.findAllByStatusAndEndTimeBefore("ACTIVE", LocalDateTime.now());

        boolean updated = false;
        for (Auction auction : expiredAuctions) {
            try {
                // Use hardcoded "STANDARD" instead of getAuctionType()
                String auctionType = "STANDARD";
                FinalizationStrategy strategy = finalizationStrategies.stream()
                        .filter(s -> s.getSupportedAuctionType().equalsIgnoreCase(auctionType))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No finalization strategy found for type: " + auctionType));

                strategy.finalizeAuction(auction);
                Auction savedAuction = auctionRepository.save(auction);
                updated = true;

                // Broadcast status update via WebSocket
                broadcastAuctionStatusUpdate(savedAuction);

            } catch (Exception e) {
                logger.error("Error finalizing auction {}: {}", auction.getAuctionId(), e.getMessage(), e);
            }
        }

        // If any auctions were updated, broadcast dashboard update
        if (updated) {
            reportService.broadcastDashboardUpdate();
        }
    }

    /**
     * Broadcasts auction status updates to all connected WebSocket clients
     */
    private void broadcastAuctionStatusUpdate(Auction auction) {
        try {
            AuctionStatusUpdateDto statusUpdate = new AuctionStatusUpdateDto(
                    auction.getAuctionId(),
                    auction.getStatus()
            );

            logger.info("Broadcasting status update for auction ID {}: {}", auction.getAuctionId(), auction.getStatus());
            messagingTemplate.convertAndSend("/topic/auction-status", statusUpdate);
        } catch (Exception e) {
            logger.error("Failed to broadcast status update for auction {}: {}", auction.getAuctionId(), e.getMessage());
        }
    }

    public AuctionChartDataDto getAuctionChartData(Long auctionId) {
        // Use findById instead of findByIdWithImages if the custom method doesn't exist
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        List<Bid> topBids = bidRepository.findTop5ByAuctionOrderByBidAmountDesc(auction);

        List<String> labels = topBids.stream()
                .map(bid -> bid.getBuyer().getFirstName() + " " + bid.getBuyer().getLastName())
                .collect(Collectors.toList());
        List<BigDecimal> data = topBids.stream()
                .map(Bid::getBidAmount)
                .collect(Collectors.toList());

        return new AuctionChartDataDto(labels, data);
    }

    public CurrentHighestBidDto getCurrentHighestBid(Long auctionId) {
        // Use findById instead of findByIdWithImages if the custom method doesn't exist
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        Optional<Bid> highestBidOpt = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction);

        String currentUserEmail = "anonymousUser";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            currentUserEmail = authentication.getName();
        }

        if (highestBidOpt.isPresent()) {
            Bid highestBid = highestBidOpt.get();
            boolean isCurrentUserHighest = highestBid.getBuyer().getEmail().equals(currentUserEmail);
            return new CurrentHighestBidDto(
                    highestBid.getBidAmount(),
                    highestBid.getBuyer().getFirstName(),
                    isCurrentUserHighest,
                    highestBid.getBidId()
            );
        } else {
            return new CurrentHighestBidDto(
                    auction.getStartPrice(),
                    "No bids yet",
                    false,
                    null
            );
        }
    }

    public SellerAuctionDetailDto getAdminAuctionDetails(Long auctionId) {
        // Use findById instead of findByIdWithImages if the custom method doesn't exist
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found with ID: " + auctionId));
        List<Bid> bids = bidRepository.findAllByAuctionOrderByBidAmountDesc(auction);
        return SellerAuctionDetailDto.fromEntity(auction, bids);
    }
}
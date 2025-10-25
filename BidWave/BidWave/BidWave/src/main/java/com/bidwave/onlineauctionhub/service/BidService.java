package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.dto.PlaceBidRequest;
import com.bidwave.onlineauctionhub.dto.UpdateBidRequest;
import com.bidwave.onlineauctionhub.dto.MyBidDto;
import com.bidwave.onlineauctionhub.models.*;
import com.bidwave.onlineauctionhub.repositories.*;
import com.bidwave.onlineauctionhub.service.bidding.BidValidationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BidService {

    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final BidValidationStrategy validationStrategy;

    @Autowired
    public BidService(
            BidRepository bidRepository,
            UserRepository userRepository,
            AuctionRepository auctionRepository,
            EmailService emailService,
            NotificationService notificationService,
            BidValidationStrategy validationStrategy
    ) {
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
        this.auctionRepository = auctionRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.validationStrategy = validationStrategy;
    }

    @Transactional
    public Bid placeBid(PlaceBidRequest request, String buyerEmail) {
        Auction auction = auctionRepository.findById(request.auctionId())
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        Buyer buyer = (Buyer) userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Buyer not found"));

        Optional<Bid> highestBidOpt = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction);

        // validation strategy
        validationStrategy.validate(auction, request.amount(), highestBidOpt);

        // Validate that buyer is not the seller
        if (auction.getSeller().getUserId().equals(buyer.getUserId())) {
            throw new IllegalArgumentException("You cannot bid on your own auction.");
        }

        // --- START: Notification Logic ---
        Seller seller = auction.getSeller();
        String sellerMessage = "A new bid of $" + request.amount() + " was placed on your item '" + auction.getItemName() + "'.";
        notificationService.createNotification(seller, sellerMessage);

        // Notify previous highest bidder if they were outbid
        highestBidOpt.ifPresent(previousHighestBid -> {
            User outbidUser = previousHighestBid.getBuyer();
            if (!outbidUser.getEmail().equals(buyerEmail)) {
                String message = "You have been outbid on '" + auction.getItemName() + "'. The new bid is $" + request.amount();
                notificationService.createNotification(outbidUser, message);
                emailService.sendOutbidNotification(outbidUser.getEmail(), auction.getItemName(), request.amount());
            }
        });
        // --- END: Notification Logic ---

        // Save the new bid
        Bid newBid = new Bid();
        newBid.setBidAmount(request.amount());
        newBid.setBidTime(LocalDateTime.now());
        newBid.setAuction(auction);
        newBid.setBuyer(buyer);

        Bid savedBid = bidRepository.save(newBid);

        // Notify bidder that their bid was placed
        notificationService.createNotification(
                buyer,
                "Your bid of $" + request.amount() + " has been placed on auction: " + auction.getItemName()
        );

        return savedBid;
    }

    public Bid updateBid(Long bidId, UpdateBidRequest request, String buyerEmail) {
        Bid existingBid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        if (!existingBid.getBuyer().getEmail().equals(buyerEmail)) {
            throw new AccessDeniedException("You can only update your own bid.");
        }
        if (existingBid.getAuction().getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Auction has ended.");
        }

        Optional<Bid> currentHighestBidOpt = bidRepository.findTopByAuctionOrderByBidAmountDesc(existingBid.getAuction());

        // ✅ FIXED: Remove the buyer parameter - validation strategy only needs 3 parameters
        validationStrategy.validate(existingBid.getAuction(), request.newAmount(), currentHighestBidOpt);

        existingBid.setBidAmount(request.newAmount());
        existingBid.setBidTime(LocalDateTime.now());

        Bid updatedBid = bidRepository.save(existingBid);

        // --- START: Notification Logic ---
        Seller seller = existingBid.getAuction().getSeller();
        String sellerMessage = "A bid was updated to $" + request.newAmount() + " on your item '" + existingBid.getAuction().getItemName() + "'.";
        notificationService.createNotification(seller, sellerMessage);

        currentHighestBidOpt.ifPresent(previousHighestBid -> {
            User outbidUser = previousHighestBid.getBuyer();
            if (!outbidUser.getEmail().equals(buyerEmail)) {
                String message = "You have been outbid on '" + existingBid.getAuction().getItemName() +
                        "'. The new bid is $" + request.newAmount();
                notificationService.createNotification(outbidUser, message);
                emailService.sendOutbidNotification(outbidUser.getEmail(), existingBid.getAuction().getItemName(), request.newAmount());
            }
        });
        // --- END: Notification Logic ---

        notificationService.createNotification(
                existingBid.getBuyer(),
                "Your bid has been updated to $" + request.newAmount() + " on auction: " + existingBid.getAuction().getItemName()
        );

        return updatedBid;
    }

    public void deleteBid(Long bidId, String buyerEmail) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        if (!bid.getBuyer().getEmail().equals(buyerEmail)) {
            throw new AccessDeniedException("You do not have permission to delete this bid.");
        }
        if (bid.getAuction().getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot delete bid: Auction has already ended.");
        }

        bidRepository.delete(bid);

        notificationService.createNotification(
                bid.getBuyer(),
                "Your bid on auction '" + bid.getAuction().getItemName() + "' has been removed."
        );

        Seller seller = bid.getAuction().getSeller();
        String sellerMessage = "A bid of $" + bid.getBidAmount() + " was removed from your item '" + bid.getAuction().getItemName() + "'.";
        notificationService.createNotification(seller, sellerMessage);
    }

    public List<MyBidDto> getBidsForBuyer(String buyerEmail) {
        Buyer buyer = (Buyer) userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Buyer not found"));

        List<Bid> bids = bidRepository.findAllByBuyerOrderByBidTimeDesc(buyer);

        return bids.stream()
                .map(MyBidDto::fromEntity)
                .collect(Collectors.toList());
    }
}
package com.bidwave.onlineauctionhub.dto;

import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.Bid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

// DTO for the main admin auction list
public record AdminAuctionViewDto(
        Long auctionId,
        String  title,
        String sellerName,
        LocalDateTime endDate,
        BigDecimal highestBid,
        int numberOfBidders,
        String status
) {
    public static AdminAuctionViewDto fromEntity(Auction auction) {
        Optional<Bid> highestBidOpt = auction.getBids().stream()
                .max((b1, b2) -> b1.getBidAmount().compareTo(b2.getBidAmount()));

        BigDecimal highestBid = highestBidOpt.map(Bid::getBidAmount).orElse(auction.getStartPrice());

        long distinctBidders = auction.getBids().stream()
                .map(bid -> bid.getBuyer().getUserId())
                .distinct()
                .count();

        return new AdminAuctionViewDto(
                auction.getAuctionId(),
                auction.getItemName(),
                auction.getSeller().getFirstName() + " " + auction.getSeller().getLastName(),
                auction.getEndTime(),
                highestBid,
                (int) distinctBidders,
                auction.getStatus()
        );
    }
}
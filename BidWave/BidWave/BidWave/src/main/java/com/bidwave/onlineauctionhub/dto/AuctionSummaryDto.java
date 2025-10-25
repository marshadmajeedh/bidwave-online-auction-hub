package com.bidwave.onlineauctionhub.dto;

import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.AuctionImage;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionSummaryDto(
        Long auctionId,
        String itemName,
        BigDecimal startPrice,
        LocalDateTime endTime,
        String status,
        String sellerName,
        int numberOfBids,
        String imageUrl
) {
    public static AuctionSummaryDto fromEntity(Auction auction) {
        String mainImageUrl = auction.getImages().stream()
                .findFirst()
                .map(AuctionImage::getImageUrl)
                .orElse(null);

        return new AuctionSummaryDto(
                auction.getAuctionId(),
                auction.getItemName(),
                auction.getStartPrice(),
                auction.getEndTime(),
                auction.getStatus(),
                auction.getSeller().getFirstName() + " " + auction.getSeller().getLastName(),
                auction.getBids().size(),
                mainImageUrl
        );
    }
}
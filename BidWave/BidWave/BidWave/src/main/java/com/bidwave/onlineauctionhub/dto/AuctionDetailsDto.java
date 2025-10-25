package com.bidwave.onlineauctionhub.dto;

import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.AuctionImage;
import com.bidwave.onlineauctionhub.models.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record AuctionDetailsDto(
        Long auctionId,
        String itemName,
        String description,
        BigDecimal startPrice,
        LocalDateTime endTime,
        String status,
        String sellerName,
        Set<String> imageUrls,
        Set<Category> categories // This is the new field
) {
    public static AuctionDetailsDto fromEntity(Auction auction) {
        return new AuctionDetailsDto(
                auction.getAuctionId(),
                auction.getItemName(),
                auction.getDescription(),
                auction.getStartPrice(),
                auction.getEndTime(),
                auction.getStatus(),
                auction.getSeller().getFirstName() + " " + auction.getSeller().getLastName(),
                auction.getImages().stream()
                        .map(AuctionImage::getImageUrl)
                        .collect(Collectors.toSet()),
                auction.getCategories() // Populate the new categories field
        );
    }
}

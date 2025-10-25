package com.bidwave.onlineauctionhub.dto;

import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.AuctionImage;
import com.bidwave.onlineauctionhub.models.Bid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record SellerAuctionDetailDto(
        AuctionDetailsDto auctionDetails,
        BigDecimal highestBid,
        List<BidderInfoDto> bidders,
        Set<String> imageUrls // <-- NEW FIELD
) {
    public static SellerAuctionDetailDto fromEntity(Auction auction, List<Bid> bids) {
        AuctionDetailsDto publicDetails = AuctionDetailsDto.fromEntity(auction);

        BigDecimal highestBid = bids.stream()
                .map(Bid::getBidAmount)
                .findFirst()
                .orElse(auction.getStartPrice());

        List<BidderInfoDto> bidderList = bids.stream()
                .map(bid -> new BidderInfoDto(
                        bid.getBuyer().getFirstName() + " " + bid.getBuyer().getLastName(),
                        bid.getBidAmount(),
                        bid.getBidTime()))
                .collect(Collectors.toList());

        Set<String> images = auction.getImages().stream()
                .map(AuctionImage::getImageUrl)
                .collect(Collectors.toSet());

        return new SellerAuctionDetailDto(publicDetails, highestBid, bidderList, images);
    }
}
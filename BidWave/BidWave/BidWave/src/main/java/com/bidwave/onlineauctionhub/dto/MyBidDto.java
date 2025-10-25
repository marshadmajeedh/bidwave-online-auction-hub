package com.bidwave.onlineauctionhub.dto;

import com.bidwave.onlineauctionhub.models.Bid;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyBidDto(
        Long bidId,
        Long auctionId,
        String itemName,
        BigDecimal bidAmount,
        LocalDateTime bidTime,
        String auctionStatus
) {
    public static MyBidDto fromEntity(Bid bid) {
        return new MyBidDto(
                bid.getBidId(),
                bid.getAuction().getAuctionId(),
                bid.getAuction().getItemName(),
                bid.getBidAmount(),
                bid.getBidTime(),
                bid.getAuction().getStatus()
        );
    }
}

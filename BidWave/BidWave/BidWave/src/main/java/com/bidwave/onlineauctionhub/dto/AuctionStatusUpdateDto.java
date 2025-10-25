package com.bidwave.onlineauctionhub.dto;

public record AuctionStatusUpdateDto(
        Long auctionId,
        String newStatus
) {}
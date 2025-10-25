package com.bidwave.onlineauctionhub.dto;

import java.math.BigDecimal;

public record CurrentHighestBidDto(
        BigDecimal amount,
        String bidderName,
        boolean isCurrentUserHighestBidder,
        Long bidId  // Add this field
) {}
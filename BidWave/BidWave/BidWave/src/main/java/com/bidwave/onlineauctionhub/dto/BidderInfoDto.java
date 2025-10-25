package com.bidwave.onlineauctionhub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidderInfoDto(
        String buyerName,
        BigDecimal bidAmount,
        LocalDateTime bidTime
) {}
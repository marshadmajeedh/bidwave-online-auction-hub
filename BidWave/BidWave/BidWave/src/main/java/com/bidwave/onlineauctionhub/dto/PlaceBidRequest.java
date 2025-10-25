package com.bidwave.onlineauctionhub.dto;

import java.math.BigDecimal;

public record PlaceBidRequest(
        Long auctionId,
        BigDecimal amount
) {}
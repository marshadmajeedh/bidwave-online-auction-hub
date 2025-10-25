package com.bidwave.onlineauctionhub.dto;

import java.math.BigDecimal;

public record UpdateBidRequest(
        BigDecimal newAmount
) {}

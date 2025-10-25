package com.bidwave.onlineauctionhub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AuctionUpdateRequest(
        String itemName,
        String description,
        BigDecimal startPrice,
        LocalDateTime endTime,
        List<Long> categoryIds
) {}
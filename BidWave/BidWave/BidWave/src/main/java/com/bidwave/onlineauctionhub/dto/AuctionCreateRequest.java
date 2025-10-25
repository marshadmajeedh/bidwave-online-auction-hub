package com.bidwave.onlineauctionhub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

// Changed categoryId from Long to List<Long>
public record AuctionCreateRequest(
        String itemName,
        String description,
        BigDecimal startPrice,
        LocalDateTime endTime,
        List<Long> categoryIds, // UPDATED
        Set<String> imageUrls
) {}
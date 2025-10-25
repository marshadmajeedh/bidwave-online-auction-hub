package com.bidwave.onlineauctionhub.dto;

import java.util.Map;

public record SystemReportDto(
        long totalUsers,
        long totalAuctions,
        Map<String, Long> auctionsByStatus
) {}
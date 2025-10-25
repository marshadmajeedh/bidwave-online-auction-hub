package com.bidwave.onlineauctionhub.dto;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {}

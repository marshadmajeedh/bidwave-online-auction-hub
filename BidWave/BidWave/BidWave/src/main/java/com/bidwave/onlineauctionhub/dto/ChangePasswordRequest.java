package com.bidwave.onlineauctionhub.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {}
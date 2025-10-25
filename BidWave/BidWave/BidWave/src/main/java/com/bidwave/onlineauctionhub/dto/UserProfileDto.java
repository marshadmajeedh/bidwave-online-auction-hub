package com.bidwave.onlineauctionhub.dto;

import com.bidwave.onlineauctionhub.models.Admin;
import com.bidwave.onlineauctionhub.models.Buyer;
import com.bidwave.onlineauctionhub.models.Seller;
import com.bidwave.onlineauctionhub.models.User;

public record UserProfileDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role,
        String status
) {
    public static UserProfileDto fromEntity(User user) {
        String role = "USER";
        if (user instanceof Admin) {
            role = "ADMIN";
        } else if (user instanceof Seller) {
            role = "SELLER";
        } else if (user instanceof Buyer) {
            role = "BUYER";
        }

        return new UserProfileDto(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                role,
                user.getStatus()
        );
    }
}
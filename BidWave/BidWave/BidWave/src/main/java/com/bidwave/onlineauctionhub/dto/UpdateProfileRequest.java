package com.bidwave.onlineauctionhub.dto;

public record UpdateProfileRequest(
        String firstName,
        String lastName
        // In the future, you could add other fields here
        // like phoneNumber or shippingAddress for a Seller.
) {}
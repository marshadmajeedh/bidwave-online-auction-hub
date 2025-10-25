package com.bidwave.onlineauctionhub.dto;

// Using a record automatically creates a class with private final fields,
// a constructor, getters, equals(), hashCode(), and toString().
public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String role // We'll expect "BUYER" or "SELLER"
) {}
package com.bidwave.onlineauctionhub.service.auth;
import org.springframework.security.core.Authentication;

/**
 * Strategy Interface for different authentication methods.
 * Defines a common contract for authenticating a user.
 */
public interface AuthenticationStrategy {
    Authentication authenticate(String username, String password);
    boolean supports(String strategyType);
}
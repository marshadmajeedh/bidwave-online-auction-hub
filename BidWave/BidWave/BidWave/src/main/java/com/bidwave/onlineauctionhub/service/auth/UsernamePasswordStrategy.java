package com.bidwave.onlineauctionhub.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy for handling standard username and password authentication.
 */
@Component
public class UsernamePasswordStrategy implements AuthenticationStrategy {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public Authentication authenticate(String username, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    @Override
    public boolean supports(String strategyType) {
        return "LOCAL".equalsIgnoreCase(strategyType);
    }
}
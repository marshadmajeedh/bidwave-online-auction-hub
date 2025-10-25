package com.bidwave.onlineauctionhub.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * The Context class that uses a specific AuthenticationStrategy.
 * It selects the appropriate strategy at runtime.
 */
@Service
public class AuthenticatorService {
    private final List<AuthenticationStrategy> authStrategies;

    @Autowired
    public AuthenticatorService(List<AuthenticationStrategy> authStrategies) {
        this.authStrategies = authStrategies;
    }

    public Authentication executeAuthentication(String username, String password, String type) {
        AuthenticationStrategy strategy = authStrategies.stream()
                .filter(s -> s.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported authentication type: " + type));

        return strategy.authenticate(username, password);
    }
}
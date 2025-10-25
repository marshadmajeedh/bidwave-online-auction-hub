package com.bidwave.onlineauctionhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC routes that anyone can access (Homepage, auth pages, etc.)
                        .requestMatchers(
                                "/", "/login", "/register", "/verify-otp", "/api/auth/**",
                                "/css/**", "/js/**"
                        ).permitAll()

                        // --- THIS RULE IS NOW REMOVED ---
                        // .requestMatchers(HttpMethod.GET, "/auctions", "/auctions/**").permitAll()

                        // We still allow anyone to VIEW a single auction detail page IF they have the direct link,
                        // but they won't be able to browse to it without logging in first.
                        .requestMatchers(HttpMethod.GET, "/auctions/*").permitAll()

                        // Authenticated routes for any logged-in user
                        .requestMatchers(
                                "/profile", "/auctions", // <-- /auctions is now an authenticated route
                                "/api/notifications/**",
                                "/api/auctions/*/highest-bid",
                                "/api/auctions/*/chart-data"
                        ).authenticated()

                        // Role-specific routes
                        .requestMatchers("/auctions/new", "/seller/**").hasRole("SELLER")
                        .requestMatchers(HttpMethod.POST, "/api/auctions").hasRole("SELLER")
                        .requestMatchers("/api/bids/**").hasRole("BUYER")
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/auctions", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}
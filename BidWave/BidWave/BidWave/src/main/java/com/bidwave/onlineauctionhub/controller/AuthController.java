package com.bidwave.onlineauctionhub.controller;

import com.bidwave.onlineauctionhub.dto.LoginRequest;
import com.bidwave.onlineauctionhub.dto.RegisterRequest;
import com.bidwave.onlineauctionhub.models.User;
import com.bidwave.onlineauctionhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterRequest registerRequest, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(registerRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please check your email to verify your account.");
            return "redirect:/login";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok("User logged in successfully!");
    }

    // --- UPDATED METHOD ---
    @GetMapping("/verify")
    public String verifyAccount(@RequestParam("token") String token) {
        try {
            userService.verifyUser(token);
            // On success, return the name of our new HTML template
            return "verification-success";
        } catch (IllegalStateException e) {
            // In case of an error (e.g., expired token), we can redirect to the login page with a message
            // For a more advanced implementation, we could create a verification-error.html page.
            return "redirect:/login?error=verification_failed";
        }
    }
}
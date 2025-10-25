package com.bidwave.onlineauctionhub.controller;

import com.bidwave.onlineauctionhub.dto.UpdateProfileRequest;
import com.bidwave.onlineauctionhub.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserActionsController {

    private final UserService userService;

    @Autowired
    public UserActionsController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UpdateProfileRequest request, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            userService.updateProfile(authentication.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Your profile has been updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "There was an error updating your profile.");
        }
        return "redirect:/profile";
    }

    // --- THIS IS THE UPDATED METHOD ---
    @PostMapping("/profile/delete")
    public String deleteProfile(Authentication authentication, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            // Try to delete the profile
            userService.deleteProfile(authentication.getName());

            // If successful, invalidate the session and log the user out
            request.getSession().invalidate();

            // --- ENHANCEMENT ---
            // Add a success message for the homepage
            redirectAttributes.addFlashAttribute("successMessage", "Your account has been successfully deleted.");
            // Redirect to the index.html page (homepage)
            return "redirect:/";
            // --- END OF ENHANCEMENT ---

        } catch (IllegalStateException e) {
            // If the service throws our business rule exception, catch it
            // and redirect back to the profile page with an error message.
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile";
        } catch (Exception e) {
            // Catch any other unexpected errors
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while deleting your profile.");
            return "redirect:/profile";
        }
    }
}
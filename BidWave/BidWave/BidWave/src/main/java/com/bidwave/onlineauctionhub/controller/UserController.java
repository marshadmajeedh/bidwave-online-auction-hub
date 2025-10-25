package com.bidwave.onlineauctionhub.controller;

import com.bidwave.onlineauctionhub.dto.ChangePasswordRequest;
import org.springframework.http.HttpStatus;
import com.bidwave.onlineauctionhub.dto.UserProfileDto;
import com.bidwave.onlineauctionhub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bidwave.onlineauctionhub.dto.UpdateProfileRequest;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me") // A common convention for the current user's profile
    public ResponseEntity<UserProfileDto> getCurrentUser() {
        UserProfileDto userProfile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateCurrentUser(@RequestBody UpdateProfileRequest updateRequest) {
        UserProfileDto updatedUserProfile = userService.updateUserProfile(updateRequest);
        return ResponseEntity.ok(updatedUserProfile);
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<String> changeCurrentUserPassword(@RequestBody ChangePasswordRequest passwordRequest) {
        try {
            userService.changeUserPassword(passwordRequest);
            return ResponseEntity.ok("Password changed successfully.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
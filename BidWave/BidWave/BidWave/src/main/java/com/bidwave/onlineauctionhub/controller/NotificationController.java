package com.bidwave.onlineauctionhub.controller;

import com.bidwave.onlineauctionhub.dto.NotificationDto;
import com.bidwave.onlineauctionhub.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(authentication.getName()));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadNotificationCount(authentication.getName())));
    }

    /**
     * Mark a single notification as read
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId, Authentication authentication) {
        notificationService.markAsRead(notificationId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all notifications as read
     */
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Clear all notifications for a user
     */
    @PostMapping("/clear-all")
    public ResponseEntity<Void> clearAll(Authentication authentication) {
        notificationService.clearAllNotifications(authentication.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a single notification
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId, Authentication authentication) {
        notificationService.deleteNotification(notificationId, authentication.getName());
        return ResponseEntity.ok().build();
    }
}
package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.dto.NotificationDto;
import com.bidwave.onlineauctionhub.models.Notification;
import com.bidwave.onlineauctionhub.models.User;
import com.bidwave.onlineauctionhub.repositories.NotificationRepository;
import com.bidwave.onlineauctionhub.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void createNotification(User user, String message) {
        Notification notification = new Notification(user, message);
        notificationRepository.save(notification);

        // Send real-time notification
        try {
            NotificationDto notificationDto = new NotificationDto(
                    notification.getId(),
                    notification.getMessage(),
                    notification.isRead(),
                    notification.getCreatedAt()
            );

            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/notifications",
                    notificationDto
            );
        } catch (Exception e) {
            // Log error but don't break the main functionality
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }
    }

    public List<NotificationDto> getNotificationsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(n -> new NotificationDto(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public long getUnreadNotificationCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Mark a single notification as read
     */
    public void markAsRead(Long notificationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Security check: ensure the notification belongs to the user
        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("You do not have permission to modify this notification.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Mark all unread notifications as read
     */
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Notification> unread = notificationRepository.findByUserAndIsReadFalse(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    /**
     * Clear all notifications for a user
     */
    public void clearAllNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        notificationRepository.deleteAll(notifications);
    }

    /**
     * Delete a single notification
     */
    public void deleteNotification(Long notificationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Security check: ensure the notification belongs to the user
        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("You do not have permission to delete this notification.");
        }

        notificationRepository.delete(notification);
    }
}
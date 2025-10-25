package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.dto.NotificationDto;
import com.bidwave.onlineauctionhub.models.Notification;
import com.bidwave.onlineauctionhub.models.User;
import com.bidwave.onlineauctionhub.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationStrategy implements NotificationStrategy {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotification(User user, String subject, String message) {
        // 1. Create and save the notification entity as before
        Notification notification = new Notification(user, subject + ": " + message);
        notificationRepository.save(notification);

        // --- THIS IS THE FIX ---
        // 2. Create a clean DTO with only the data we need
        NotificationDto notificationDto = new NotificationDto(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
        // --- END OF FIX ---

        // 3. Push the clean DTO over the WebSocket, not the raw entity
        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/notifications",
                notificationDto
        );
    }
}
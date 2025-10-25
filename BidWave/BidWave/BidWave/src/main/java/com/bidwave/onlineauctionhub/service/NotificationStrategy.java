package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.models.User;

public interface NotificationStrategy {
    void sendNotification(User user, String subject, String message);
}
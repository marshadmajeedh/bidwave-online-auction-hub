package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationSender {
    private final List<NotificationStrategy> strategies;
    @Autowired
    public NotificationSender(List<NotificationStrategy> strategies) {
        this.strategies = strategies;
    }
    public void send(User user, String subject, String message) {
        strategies.forEach(strategy -> strategy.sendNotification(user, subject, message));
    }
}
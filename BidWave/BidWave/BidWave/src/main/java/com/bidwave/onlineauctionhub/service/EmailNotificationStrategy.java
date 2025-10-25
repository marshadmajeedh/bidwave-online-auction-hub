package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

//... imports
@Component
public class EmailNotificationStrategy implements NotificationStrategy {
    @Autowired
    private JavaMailSender mailSender;
    @Override
    public void sendNotification(User user, String subject, String message) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }
}
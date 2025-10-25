package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.models.Auction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "BidWave Account Verification";
        String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + token;
        String message = "Thank you for registering. Please click the link below to verify your account:\n" + verificationUrl;
        sendEmail(toEmail, subject, message);
    }

    public void sendOutbidNotification(String toEmail, String itemName, BigDecimal newBidAmount) {
        String subject = "You've been outbid on " + itemName + "!";
        String message = "Hello,\n\nA higher bid has been placed on the item '" + itemName +
                "'.\nThe new highest bid is $" + newBidAmount +
                ".\n\nVisit BidWave now to place a new bid!\n\nThank you,\nThe BidWave Team";
        sendEmail(toEmail, subject, message);
    }

    public void sendWinnerNotification(String winnerEmail, String itemName, BigDecimal winningAmount) {
        String subject = "Congratulations! You won the auction for " + itemName;
        String message = "Hello,\n\nCongratulations! You have placed the winning bid for the item '" + itemName +
                "'.\n\nYour winning bid was: $" + winningAmount +
                "\n\nThe seller will be in contact with you shortly to arrange payment and delivery." +
                "\n\nThank you for using BidWave,\nThe BidWave Team";
        sendEmail(winnerEmail, subject, message);
    }

    public void sendSellerNotification(String sellerEmail, String itemName, BigDecimal finalAmount, String winnerEmail) {
        String subject = "Your auction for '" + itemName + "' has ended";
        String message;
        if (finalAmount != null && winnerEmail != null) {
            message = "Hello,\n\nGreat news! Your auction for the item '" + itemName +
                    "' has ended successfully.\n\nIt was sold for: $" + finalAmount +
                    "\n\nThe winning bidder is: " + winnerEmail +
                    "\n\nPlease contact the buyer to arrange payment and delivery." +
                    "\n\nThank you for selling with BidWave,\nThe BidWave Team";
        } else {
            message = "Hello,\n\nYour auction for '" + itemName +
                    "' has ended without any bids. The item was not sold." +
                    "\n\nThank you for selling with BidWave,\nThe BidWave Team";
        }
        sendEmail(sellerEmail, subject, message);
    }

    public void sendAdminApprovalNotification(String adminEmail, Auction auction) {
        String subject = "New Auction Pending Approval: " + auction.getItemName();
        String message = "Hello Admin,\n\nA new auction has been created by seller '" + auction.getSeller().getEmail() +
                "' and is waiting for your approval.\n\nItem: " + auction.getItemName() +
                "\nAuction ID: " + auction.getAuctionId() +
                "\n\nPlease review it in the admin dashboard.";
        sendEmail(adminEmail, subject, message);
    }

    public void sendSellerAuctionApprovedNotification(String sellerEmail, Auction auction) {
        String subject = "Congratulations! Your auction for '" + auction.getItemName() + "' has been approved";
        String message = "Hello,\n\nYour auction for '" + auction.getItemName() +
                "' has been approved by an administrator and is now live for bidding." +
                "\n\nThank you for selling with BidWave,\nThe BidWave Team";
        sendEmail(sellerEmail, subject, message);
    }

    public void sendNewAuctionNotificationToBuyer(String buyerEmail, Auction auction) {
        String subject = "New Auction Alert: " + auction.getItemName();
        String message = "Hello,\n\nA new item is up for auction that you might be interested in:\n\nItem: " + auction.getItemName() +
                "\nStarting Price: $" + auction.getStartPrice() +
                "\n\nHappy bidding!\nThe BidWave Team";
        sendEmail(buyerEmail, subject, message);
    }

    public void sendSellerAuctionDisapprovedNotification(String sellerEmail, Auction auction) {
        String subject = "Update on your auction for '" + auction.getItemName() + "'";
        String message = "Hello,\n\nWe have reviewed your auction for '" + auction.getItemName() +
                "'. Unfortunately, it did not meet our listing criteria and has been disapproved." +
                "\n\nPlease contact support if you have any questions.\nThe BidWave Team";
        sendEmail(sellerEmail, subject, message);
    }

    public void sendSellerAuctionUnsoldNotification(String sellerEmail, Auction auction) {
        String subject = "Your auction for '" + auction.getItemName() + "' has ended";
        String message = "Hello,\n\nYour auction for '" + auction.getItemName() +
                "' has ended without any bids. The item was not sold." +
                "\n\nThank you for selling with BidWave,\nThe BidWave Team";
        sendEmail(sellerEmail, subject, message);
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(text);
        mailSender.send(email);
    }
}
package com.bidwave.onlineauctionhub.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auction_reports")
public class AuctionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_user_id")
    private User winner;

    private BigDecimal finalBidAmount;

    private LocalDateTime generatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id")
    private User createdBy; // Changed from Admin to User since Admin likely extends User

    // Default constructor (required by JPA)
    public AuctionReport() {}

    // Parameterized constructor
    public AuctionReport(Auction auction, User winner, BigDecimal finalBidAmount, User createdBy) {
        this.auction = auction;
        this.winner = winner;
        this.finalBidAmount = finalBidAmount;
        this.createdBy = createdBy;
        this.generatedDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public BigDecimal getFinalBidAmount() {
        return finalBidAmount;
    }

    public void setFinalBidAmount(BigDecimal finalBidAmount) {
        this.finalBidAmount = finalBidAmount;
    }

    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "AuctionReport{" +
                "reportId=" + reportId +
                ", auction=" + (auction != null ? auction.getAuctionId() : "null") +
                ", winner=" + (winner != null ? winner.getUserId() : "null") +
                ", finalBidAmount=" + finalBidAmount +
                ", generatedDate=" + generatedDate +
                ", createdBy=" + (createdBy != null ? createdBy.getUserId() : "null") +
                '}';
    }
}
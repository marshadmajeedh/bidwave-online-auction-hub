package com.bidwave.onlineauctionhub.models;

import jakarta.persistence.*;

@Entity
@Table(name = "auction_images")
public class AuctionImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // The required Primary Key

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // This creates the link back to the Auction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    // --- Constructors, Getters, and Setters ---
    public AuctionImage() {}

    public AuctionImage(String imageUrl, Auction auction) {
        this.imageUrl = imageUrl;
        this.auction = auction;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public Auction getAuction() {
        return auction;
    }
    public void setAuction(Auction auction) {
        this.auction = auction;
    }
}

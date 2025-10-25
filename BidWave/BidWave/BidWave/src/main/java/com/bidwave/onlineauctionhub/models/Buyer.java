package com.bidwave.onlineauctionhub.models;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("BUYER")
public class Buyer extends User {

    @Column(name = "billing_address") // Explicitly name the column in the database
    private String billingAddress; // Corresponds to billingAddress attribute [cite: 832]

    // Constructors, Getters, and Setters
    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }
}
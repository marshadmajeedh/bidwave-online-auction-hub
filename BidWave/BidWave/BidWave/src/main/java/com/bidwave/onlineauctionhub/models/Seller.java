package com.bidwave.onlineauctionhub.models;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("SELLER")
public class Seller extends User {

    @Column(name = "phone_number")
    private String phoneNumber; // Corresponds to phoneNumber attribute [cite: 820]

    @Column(name = "shipping_address")
    private String shippingAddress; // Corresponds to shippingAddress attribute [cite: 829]

    @Column(name = "business_reg_number")
    private String businessRegistrationNumber; // Corresponds to businessRegistrationNumber attribute [cite: 816]

    @Column(name = "id_verification_status")
    private String idVerificationStatus; // Corresponds to idVerificationStatus attribute [cite: 815]

    // Constructors, Getters, and Setters
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getBusinessRegistrationNumber() {
        return businessRegistrationNumber;
    }

    public void setBusinessRegistrationNumber(String businessRegistrationNumber) {
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    public String getIdVerificationStatus() {
        return idVerificationStatus;
    }

    public void setIdVerificationStatus(String idVerificationStatus) {
        this.idVerificationStatus = idVerificationStatus;
    }

}
package com.bidwave.onlineauctionhub.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN") // This value will be stored in the user_role column
public class
Admin extends User {
    // This class can be empty for now as Admins do not have extra fields
    // compared to a base User, but its existence models the role correctly.
}
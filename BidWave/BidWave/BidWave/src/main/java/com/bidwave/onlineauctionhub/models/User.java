package com.bidwave.onlineauctionhub.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // This annotation is optional if table name matches class name
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // All user types in one table
@DiscriminatorColumn(name="user_role", discriminatorType = DiscriminatorType.STRING) // Column to identify role
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; // Corresponds to userID in EER [cite: 625]

    @Column(nullable = false, unique = true)
    private String email; // Corresponds to email [cite: 628]

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false; // Default to false until verified

    @Column(nullable = false)
    private String password; // Will be stored encrypted

    private String firstName; // Corresponds to firstName [cite: 621]
    private String lastName; // Corresponds to lastName [cite: 622]

    private String status; // E.g., "active", "suspended" [cite: 624]

    private LocalDateTime registrationDate; // Corresponds to registrationDate [cite: 629]
    private LocalDateTime lastLogin; // Corresponds to lastLogin [cite: 627]

    public Long getUserId() {
        return userId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // Getters and Setters
}
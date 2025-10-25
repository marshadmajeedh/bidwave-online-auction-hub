package com.bidwave.onlineauctionhub.models;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;// Corresponds to categoryID [cite: 992]

    @Column(nullable = false, unique = true)
    private String name;// Corresponds to name [cite: 992]

    public Long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setName(String name) {
        this.name = name;
    }
    // Getters and Setters
}
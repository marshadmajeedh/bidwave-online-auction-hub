package com.bidwave.onlineauctionhub.models;

public enum TargetAudience {
    ALL("All Users"),
    BUYERS("Buyers Only"),
    SELLERS("Sellers Only");

    private final String displayName;

    TargetAudience(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

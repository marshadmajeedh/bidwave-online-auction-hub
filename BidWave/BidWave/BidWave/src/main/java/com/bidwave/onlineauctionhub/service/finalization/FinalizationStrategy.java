package com.bidwave.onlineauctionhub.service.finalization;

import com.bidwave.onlineauctionhub.models.Auction;

public interface FinalizationStrategy {
    void finalizeAuction(Auction auction);
    String getSupportedAuctionType(); // e.g., "STANDARD", "RESERVE"
}
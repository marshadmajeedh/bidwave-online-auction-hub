package com.bidwave.onlineauctionhub.service.bidding;

import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.Bid;
import java.math.BigDecimal;
import java.util.Optional;

public interface BidValidationStrategy {
    void validate(Auction auction, BigDecimal newBidAmount, Optional<Bid> highestBid);
}
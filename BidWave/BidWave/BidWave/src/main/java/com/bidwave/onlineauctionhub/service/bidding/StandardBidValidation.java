package com.bidwave.onlineauctionhub.service.bidding;

import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.Bid;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class StandardBidValidation implements BidValidationStrategy {
    @Override
    public void validate(Auction auction, BigDecimal newBidAmount, Optional<Bid> highestBid) {
        if (auction.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Auction has already ended.");
        }
        BigDecimal currentHighest = highestBid.map(Bid::getBidAmount).orElse(auction.getStartPrice());
        if (newBidAmount.compareTo(currentHighest) <= 0) {
            throw new IllegalArgumentException("Your bid must be higher than the current highest bid of $" + currentHighest);
        }
    }
}
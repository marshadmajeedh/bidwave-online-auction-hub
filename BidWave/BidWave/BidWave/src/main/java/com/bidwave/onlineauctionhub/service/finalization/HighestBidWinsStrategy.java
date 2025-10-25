package com.bidwave.onlineauctionhub.service.finalization;

import com.bidwave.onlineauctionhub.models.*;
import com.bidwave.onlineauctionhub.repositories.BidRepository;
import com.bidwave.onlineauctionhub.service.NotificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HighestBidWinsStrategy implements FinalizationStrategy {
    @Autowired private BidRepository bidRepository;
    @Autowired private NotificationSender notificationSender;

    @Override
    public void finalizeAuction(Auction auction) {
        bidRepository.findTopByAuctionOrderByBidAmountDesc(auction).ifPresentOrElse(
                winningBid -> {
                    auction.setStatus("CLOSED_SOLD");
                    // Notify winner and seller
                    notificationSender.send(winningBid.getBuyer(), "You won!", "Congratulations, you won " + auction.getItemName());
                    notificationSender.send(auction.getSeller(), "Item Sold!", "Your item " + auction.getItemName() + " was sold.");
                },
                () -> {
                    auction.setStatus("CLOSED_UNSOLD");
                    notificationSender.send(auction.getSeller(), "Auction Ended", "Your auction for " + auction.getItemName() + " ended without bids.");
                }
        );
    }

    @Override
    public String getSupportedAuctionType() {
        return "STANDARD";
    }
}
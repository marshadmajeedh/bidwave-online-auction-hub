package com.bidwave.onlineauctionhub.repositories;

import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.Bid;
import com.bidwave.onlineauctionhub.models.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findTopByAuctionOrderByBidAmountDesc(Auction auction);

    List<Bid> findAllByAuctionOrderByBidAmountDesc(Auction auction);

    List<Bid> findAllByAuctionOrderByBidTimeAsc(Auction auction);

    long countByBuyer(Buyer buyer);

    long countByAuction(Auction auction);

    // Finds the Top 5 bids for an auction, ordered by amount descending
    List<Bid> findTop5ByAuctionOrderByBidAmountDesc(Auction auction);

    // --- NEW METHOD ---
    // Finds all bids by a specific buyer, ordered by most recent first
    List<Bid> findAllByBuyerOrderByBidTimeDesc(Buyer buyer);
}
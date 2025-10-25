package com.bidwave.onlineauctionhub.repositories;

import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.AuctionReport;
import com.bidwave.onlineauctionhub.models.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, Long>, JpaSpecificationExecutor<Auction> {

    // Keep all existing methods from both versions
    List<Auction> findAllBySeller(Seller seller);
    List<Auction> findAllByStatus(String status);
    List<Auction> findAllByStatusIn(List<String> statuses);

    // Custom query for filtering by statuses and category
    @Query("SELECT DISTINCT a FROM Auction a JOIN a.categories c WHERE a.status IN :statuses AND c.categoryId = :categoryId")
    List<Auction> findAllByStatusInAndCategories_CategoryId(List<String> statuses, Long categoryId);

    // Find expired auctions by status
    List<Auction> findAllByStatusAndEndTimeBefore(String status, LocalDateTime currentTime);

    // Count auctions grouped by status
    @Query("SELECT a.status, COUNT(a) FROM Auction a GROUP BY a.status")
    List<Object[]> countAuctionsByStatus();

    // Count auctions by seller
    long countBySeller(Seller seller);
}
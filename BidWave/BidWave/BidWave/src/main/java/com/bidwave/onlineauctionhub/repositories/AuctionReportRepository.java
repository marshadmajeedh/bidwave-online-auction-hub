package com.bidwave.onlineauctionhub.repositories;

import com.bidwave.onlineauctionhub.models.AuctionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuctionReportRepository extends JpaRepository<AuctionReport, Long> {
    // Find all reports, newest first
    List<AuctionReport> findAllByOrderByGeneratedDateDesc();
    boolean existsByAuction_AuctionId(Long auctionId);
}

package com.bidwave.onlineauctionhub.controller;

import com.bidwave.onlineauctionhub.dto.AuctionChartDataDto;
import com.bidwave.onlineauctionhub.dto.CurrentHighestBidDto;
import com.bidwave.onlineauctionhub.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auctions")
public class AuctionApiController {

    @Autowired
    private AuctionService auctionService;

    /**
     * API endpoint for JavaScript to get the current highest bid for the real-time detail page.
     */
    @GetMapping("/{id}/highest-bid")
    public ResponseEntity<CurrentHighestBidDto> getHighestBid(@PathVariable Long id) {
        try {
            CurrentHighestBidDto highestBid = auctionService.getCurrentHighestBid(id);
            return ResponseEntity.ok(highestBid);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * API endpoint for JavaScript to get data for the seller's monitoring chart.
     */
    @GetMapping("/{id}/chart-data")
    public ResponseEntity<AuctionChartDataDto> getAuctionChartData(@PathVariable Long id) {
        try {
            AuctionChartDataDto chartData = auctionService.getAuctionChartData(id);
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            // Log the error in a real application
            // e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
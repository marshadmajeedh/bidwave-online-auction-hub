package com.bidwave.onlineauctionhub.controller;

import com.bidwave.onlineauctionhub.dto.AuctionCreateRequest;
import com.bidwave.onlineauctionhub.dto.AuctionSummaryDto;
import com.bidwave.onlineauctionhub.dto.AuctionDetailsDto;
import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/api/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    /**
     * Create a new auction with uploaded image files
     */
    @PostMapping
    public String createAuction(@ModelAttribute("auctionForm") AuctionCreateRequest request,
                                @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        String sellerEmail = authentication.getName();
        try {
            Auction newAuction = auctionService.createAuction(request, imageFiles, sellerEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Auction created successfully and is now pending approval.");
            return "redirect:/auctions/" + newAuction.getAuctionId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating auction: " + e.getMessage());
            return "redirect:/auctions/new";
        }
    }

    /**
     * Get all auctions created by the currently authenticated seller
     */
    @GetMapping("/my-listings")
    public ResponseEntity<List<AuctionSummaryDto>> getMyListings(Authentication authentication) {
        String sellerEmail = authentication.getName();
        List<AuctionSummaryDto> listings = auctionService.getAuctionsForSeller(sellerEmail);
        return ResponseEntity.ok(listings);
    }

    /**
     * Get all active auctions (default sorted by ending soon)
     */
    @GetMapping
    public ResponseEntity<List<AuctionSummaryDto>> getAllActiveAuctions() {
        return ResponseEntity.ok(auctionService.getActiveAuctions(null, null, null, null, "ending_soon"));
    }

    /**
     * Get details for a specific auction by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuctionDetailsDto> getAuctionById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(auctionService.getAuctionDetails(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

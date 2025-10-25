package com.bidwave.onlineauctionhub.controller;

import com.bidwave.onlineauctionhub.dto.AuctionUpdateRequest;
import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.Bid;
import com.bidwave.onlineauctionhub.repositories.AuctionRepository;
import com.bidwave.onlineauctionhub.repositories.BidRepository;
import com.bidwave.onlineauctionhub.service.AuctionService;
import com.bidwave.onlineauctionhub.service.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/seller/auctions") // All routes here start with /seller/auctions
public class SellerController {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    // === Existing Methods ===

    @PostMapping("/{auctionId}/delete")
    public String deleteAuction(@PathVariable Long auctionId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            auctionService.deleteSellerAuction(auctionId, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Auction deleted successfully.");
            return "redirect:/seller/dashboard"; // Redirect to the seller's main dashboard
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting auction: " + e.getMessage());
            return "redirect:/seller/monitor/" + auctionId;
        }
    }

    @PostMapping("/{auctionId}/update")
    public String updateAuction(@PathVariable Long auctionId,
                                @ModelAttribute AuctionUpdateRequest request,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            auctionService.updateSellerAuction(auctionId, request, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Auction updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating auction: " + e.getMessage());
        }
        return "redirect:/seller/monitor/" + auctionId;
    }

    // === New Method: PDF Report Generation ===

    @GetMapping("/{auctionId}/report")
    public ResponseEntity<InputStreamResource> downloadSellerAuctionReport(@PathVariable Long auctionId, Authentication authentication) {
        // Security check to ensure the seller owns the auction
        auctionService.getSellerAuctionDetails(auctionId, authentication.getName());

        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new RuntimeException("Auction not found"));
        List<Bid> bids = bidRepository.findAllByAuctionOrderByBidAmountDesc(auction);

        ByteArrayInputStream bis = pdfGenerationService.generateAuctionBiddersReportPdf(auction, bids);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=bidders_report_" + auctionId + ".pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
    }
}

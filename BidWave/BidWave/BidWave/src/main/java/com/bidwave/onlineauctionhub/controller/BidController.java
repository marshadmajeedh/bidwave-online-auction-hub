package com.bidwave.onlineauctionhub.controller;

import com.bidwave.onlineauctionhub.dto.PlaceBidRequest;
import com.bidwave.onlineauctionhub.dto.UpdateBidRequest;
import com.bidwave.onlineauctionhub.repositories.BidRepository;
import com.bidwave.onlineauctionhub.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/bids")
public class BidController {

    @Autowired
    private BidService bidService;

    @Autowired
    private BidRepository bidRepository;

    @PostMapping
    public String placeBid(@ModelAttribute PlaceBidRequest request, Authentication authentication, RedirectAttributes redirectAttributes) {
        String redirectUrl = "redirect:/auctions/" + request.auctionId();
        try {
            String buyerEmail = authentication.getName();
            bidService.placeBid(request, buyerEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Your bid was placed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bid failed: " + e.getMessage());
        }
        return redirectUrl;
    }

    @PostMapping("/{bidId}/update")
    public String updateBid(@PathVariable Long bidId, @ModelAttribute UpdateBidRequest request, Authentication authentication, RedirectAttributes redirectAttributes) {
        String buyerEmail = authentication.getName();
        Long auctionId = bidRepository.findById(bidId).get().getAuction().getAuctionId();
        try {
            bidService.updateBid(bidId, request, buyerEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Your bid was updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bid update failed: " + e.getMessage());
        }
        return "redirect:/auctions/" + auctionId;
    }

    @PostMapping("/{bidId}/delete")
    public String deleteBid(@PathVariable Long bidId, Authentication authentication, RedirectAttributes redirectAttributes) {
        String buyerEmail = authentication.getName();
        try {
            bidService.deleteBid(bidId, buyerEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Your bid has been successfully retracted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not retract bid: " + e.getMessage());
        }
        // Corrected redirect to the main auctions page
        return "redirect:/auctions";
    }
}
package com.bidwave.onlineauctionhub.controller;

import com.bidwave.onlineauctionhub.dto.*;
import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.AuctionReport;
import com.bidwave.onlineauctionhub.models.Bid;
import com.bidwave.onlineauctionhub.models.TargetAudience;
import com.bidwave.onlineauctionhub.repositories.AuctionReportRepository;
import com.bidwave.onlineauctionhub.repositories.AuctionRepository;
import com.bidwave.onlineauctionhub.repositories.BidRepository;
import com.bidwave.onlineauctionhub.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final AuctionService auctionService;
    private final ReportService reportService;
    private final PdfGenerationService pdfGenerationService;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final AuctionReportRepository reportRepository;
    private final AnnouncementService announcementService;

    @Autowired
    public AdminController(UserService userService, AuctionService auctionService,
                           ReportService reportService, PdfGenerationService pdfGenerationService,
                           AuctionRepository auctionRepository, BidRepository bidRepository,
                           AuctionReportRepository reportRepository, AnnouncementService announcementService) {
        this.userService = userService;
        this.auctionService = auctionService;
        this.reportService = reportService;
        this.pdfGenerationService = pdfGenerationService;
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.reportRepository = reportRepository;
        this.announcementService = announcementService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileDto>> getAllUsers() {
        List<UserProfileDto> allUsers = userService.getAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserProfileDto> updateUserStatus(@PathVariable Long userId, @RequestBody UpdateUserStatusRequest statusRequest) {
        try {
            UserProfileDto updatedUser = userService.updateUserStatus(userId, statusRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/users/{userId}/status")
    public String updateUserStatusFromForm(@PathVariable Long userId, @RequestParam String status, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserStatus(userId, new UpdateUserStatusRequest(status));
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/auctions")
    public ResponseEntity<List<AdminAuctionViewDto>> getAllAuctions() {
        // Correctly type the variable and call the service method
        List<AdminAuctionViewDto> allAuctions = auctionService.getAllAuctionsForAdmin(null, null);

        // Return the response with the correct type
        return ResponseEntity.ok(allAuctions);
    }

    @PatchMapping("/auctions/{auctionId}/status")
    public ResponseEntity<AuctionDetailsDto> updateAuctionStatus(@PathVariable Long auctionId, @RequestBody UpdateAuctionStatusRequest statusRequest) {
        try {
            AuctionDetailsDto updatedAuction = auctionService.updateAuctionStatus(auctionId, statusRequest);
            return ResponseEntity.ok(updatedAuction);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/auctions/{auctionId}/status")
    public String updateAuctionStatusFromForm(@PathVariable Long auctionId, @RequestParam String status, RedirectAttributes redirectAttributes) {
        try {
            auctionService.updateAuctionStatus(auctionId, new UpdateAuctionStatusRequest(status));
            redirectAttributes.addFlashAttribute("successMessage", "Auction status updated successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update auction status.");
        }
        return "redirect:/admin/auctions";
    }

    @GetMapping("/reports/activity")
    public ResponseEntity<SystemReportDto> getSystemActivityReport() {
        SystemReportDto report = reportService.generateSystemActivityReport();
        return ResponseEntity.ok(report);
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/auctions/{auctionId}/delete")
    public String deleteAuction(@PathVariable Long auctionId, RedirectAttributes redirectAttributes) {
        try {
            auctionService.deleteAuction(auctionId);
            redirectAttributes.addFlashAttribute("successMessage", "Auction deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/auctions";
    }

    @GetMapping("/auctions/{auctionId}/report")
    public ResponseEntity<InputStreamResource> downloadWinnerReport(@PathVariable Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new RuntimeException("Auction not found"));
        Optional<Bid> winningBid = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction);

        ByteArrayInputStream bis = pdfGenerationService.generateWinnerReportPdf(auction, winningBid);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=auction_report_" + auctionId + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    // This method now CREATES a report record
    @PostMapping("/auctions/{auctionId}/finalize-report")
    public String finalizeAndCreateReport(@PathVariable Long auctionId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            reportService.createAuctionReport(auctionId, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Report successfully created and saved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating report: " + e.getMessage());
            return "redirect:/admin/auctions";
        }
        // Redirect to the new "View Reports" page to see the new record
        return "redirect:/admin/reports";
    }

    // The download endpoint now finds the report first
    @GetMapping("/reports/{reportId}/download")
    public ResponseEntity<InputStreamResource> downloadReport(@PathVariable Long reportId) {
        AuctionReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        Auction auction = report.getAuction();
        List<Bid> bids = bidRepository.findAllByAuctionOrderByBidAmountDesc(auction);

        ByteArrayInputStream bis = pdfGenerationService.generateAuctionBiddersReportPdf(auction, bids);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=auction_report_" + auction.getAuctionId() + ".pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
    }

    // === ADDED: New announcements method ===
    @PostMapping("/announcements")
    public String createAnnouncement(@RequestParam("content") String content,
                                     @RequestParam("targetAudience") TargetAudience targetAudience,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        try {
            String adminEmail = authentication.getName();
            announcementService.createAnnouncement(content, targetAudience, adminEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Announcement published successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error publishing announcement: " + e.getMessage());
        }
        return "redirect:/admin/announcements";
    }
}
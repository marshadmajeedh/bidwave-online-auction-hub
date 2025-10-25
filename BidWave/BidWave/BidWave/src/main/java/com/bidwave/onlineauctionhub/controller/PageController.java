package com.bidwave.onlineauctionhub.controller;

import com.bidwave.onlineauctionhub.dto.*;
import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.Category;
import com.bidwave.onlineauctionhub.models.TargetAudience;
import com.bidwave.onlineauctionhub.repositories.AuctionRepository;
import com.bidwave.onlineauctionhub.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class PageController {

    private final AuctionService auctionService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ReportService reportService;
    private final AuctionRepository auctionRepository;
    private final BidService bidService;
    private final AnnouncementService announcementService;

    @Autowired
    public PageController(AuctionService auctionService, UserService userService, CategoryService categoryService,
                          ReportService reportService, AuctionRepository auctionRepository, BidService bidService,
                          AnnouncementService announcementService) {
        this.auctionService = auctionService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.reportService = reportService;
        this.auctionRepository = auctionRepository;
        this.bidService = bidService;
        this.announcementService = announcementService;
    }

    @GetMapping("/")
    @Transactional(readOnly = true)
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    @Transactional(readOnly = true)
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    @Transactional(readOnly = true)
    public String register(Model model) {
        model.addAttribute("user", new RegisterRequest("", "", "", "", "BUYER"));
        return "register";
    }

    @GetMapping("/auctions")
    @Transactional(readOnly = true)
    public String listAuctions(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "ending_soon") String sort,
            Model model) {

        List<AuctionSummaryDto> auctionList = auctionService.getActiveAuctions(q, category, minPrice, maxPrice, sort);
        List<Category> categories = categoryService.findAll();

        model.addAttribute("auctions", auctionList);
        model.addAttribute("categories", categories);
        model.addAttribute("searchTerm", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedSort", sort);

        return "auctions";
    }

    @GetMapping("/auctions/{id}")
    @Transactional(readOnly = true)
    public String auctionDetail(@PathVariable Long id, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Auction auction = auctionRepository.findById(id)
                    .orElseThrow(() -> new AuctionNotFoundException("Auction not found with ID: " + id));

            // --- NEW: Add the end time as a standard ISO string ---
            model.addAttribute("auctionEndTimeISO", auction.getEndTime().toString());

            if (auth != null && auth.isAuthenticated() && auth.getName().equals(auction.getSeller().getEmail())) {
                SellerAuctionDetailDto sellerDetails = auctionService.getSellerAuctionDetails(id, auth.getName());
                model.addAttribute("sellerDetails", sellerDetails);
                model.addAttribute("auction", sellerDetails.auctionDetails());
            } else {
                AuctionDetailsDto publicDetails = auctionService.getAuctionDetails(id);
                model.addAttribute("auction", publicDetails);
            }

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            String formattedStartPrice = currencyFormat.format(auction.getStartPrice());
            model.addAttribute("formattedStartPrice", formattedStartPrice);

            model.addAttribute("bidForm", new PlaceBidRequest(id, null));
            return "auction-detail";
        } catch (AuctionNotFoundException e) {
            return "redirect:/auctions?error=" + e.getMessage();
        }
    }

    @GetMapping("/auctions/new")
    @Transactional(readOnly = true)
    public String createAuctionForm(Model model) {
        model.addAttribute("auctionForm", new AuctionCreateRequest(null, null, null, null, Collections.emptyList(), null));
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "create-auction";
    }

    @GetMapping("/seller/auctions/{auctionId}/edit")
    @PreAuthorize("hasRole('SELLER')")
    public String editAuctionForm(@PathVariable Long auctionId, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            SellerAuctionDetailDto details = auctionService.getSellerAuctionDetails(auctionId, authentication.getName());

            if (!details.bidders().isEmpty()) {
                // Redirect with an error if bids have been placed
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot edit an auction after bidding has started.");
                return "redirect:/seller/monitor/" + auctionId;
            }

            AuctionDetailsDto auctionData = details.auctionDetails();

            // Create the form-backing object with the auction's current data
            AuctionUpdateRequest formObject = new AuctionUpdateRequest(
                    auctionData.itemName(),
                    auctionData.description(),
                    auctionData.startPrice(),
                    auctionData.endTime(),
                    auctionData.categories().stream().map(Category::getCategoryId).collect(Collectors.toList())
            );

            model.addAttribute("auctionForm", formObject); // Use this object in the form
            model.addAttribute("auctionId", auctionId);
            model.addAttribute("categories", categoryService.findAll());
            return "seller-edit-auction";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unable to load auction for editing: " + e.getMessage());
            return "redirect:/seller/dashboard";
        }
    }

    @PostMapping("/seller/auctions/update/{auctionId}")
    @PreAuthorize("hasRole('SELLER')")
    public String updateAuction(@PathVariable Long auctionId,
                                @ModelAttribute AuctionUpdateRequest updateRequest,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            Auction updatedAuction = auctionService.updateSellerAuction(auctionId, updateRequest, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Auction updated successfully.");
            return "redirect:/seller/monitor/" + auctionId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/auctions/" + auctionId + "/edit";
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public String userProfile(Model model) {
        UserProfileDto userProfile = userService.getCurrentUserProfile();
        model.addAttribute("userProfile", userProfile);
        return "profile";
    }

    @GetMapping("/profile/edit")
    @PreAuthorize("isAuthenticated()")
    public String editProfilePage(Model model) {
        UserProfileDto userProfile = userService.getCurrentUserProfile();

        // Create the form-backing object with the user's current data
        UpdateProfileRequest updateRequest = new UpdateProfileRequest(
                userProfile.firstName(),
                userProfile.lastName()
        );

        model.addAttribute("updateRequest", updateRequest); // Pass this object to the form
        return "edit-profile";
    }

    @GetMapping("/seller/dashboard")
    @PreAuthorize("hasRole('SELLER')")
    @Transactional(readOnly = true)
    public String sellerDashboard(Authentication authentication, Model model, HttpServletResponse response) {
        // --- FIX: Add Cache-Control headers ---
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        // ------------------------------------

        List<AuctionSummaryDto> auctions = auctionService.getAuctionsForSeller(authentication.getName());
        model.addAttribute("auctions", auctions);
        return "seller-dashboard";
    }

    @GetMapping("/seller/monitor/{auctionId}")
    @PreAuthorize("hasRole('SELLER')")
    @Transactional(readOnly = true)
    public String monitorAuction(@PathVariable Long auctionId, Authentication authentication, Model model) {
        try {
            SellerAuctionDetailDto details = auctionService.getSellerAuctionDetails(auctionId, authentication.getName());
            AuctionChartDataDto chartData = auctionService.getAuctionChartData(auctionId);
            model.addAttribute("auctionDetails", details);
            model.addAttribute("chartData", chartData);
            return "seller-monitor-auction";
        } catch (AuctionNotFoundException | org.springframework.security.access.AccessDeniedException e) {
            return "redirect:/seller/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/seller/auctions/delete/{auctionId}")
    @PreAuthorize("hasRole('SELLER')")
    public String deleteAuction(@PathVariable Long auctionId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            auctionService.deleteSellerAuction(auctionId, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Auction deleted successfully.");
            return "redirect:/seller/dashboard";
        } catch (AuctionNotFoundException | org.springframework.security.access.AccessDeniedException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/dashboard";
        }
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String adminDashboard(Model model) {
        SystemReportDto report = reportService.generateSystemActivityReport();
        model.addAttribute("report", report);
        return "admin-dashboard";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String manageUsers(Model model) {
        List<UserProfileDto> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);
        return "admin-manage-users";
    }

    // ✅ UPDATED: Enhanced admin auctions management with filtering
    @GetMapping("/admin/auctions")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String manageAuctions(
            @RequestParam(required = false) String seller,
            @RequestParam(required = false) String status,
            Model model, HttpServletResponse response) {

        // --- FIX: Add Cache-Control headers ---
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        // ------------------------------------

        List<AdminAuctionViewDto> allAuctions = auctionService.getAllAuctionsForAdmin(seller, status);
        model.addAttribute("auctions", allAuctions);
        model.addAttribute("currentSeller", seller);
        model.addAttribute("currentStatus", status);
        return "admin-manage-auctions";
    }

    // ✅ NEW: Admin auction detail view
    @GetMapping("/admin/auctions/{auctionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String adminAuctionDetail(@PathVariable Long auctionId, Model model) {
        try {
            SellerAuctionDetailDto details = auctionService.getAdminAuctionDetails(auctionId);
            model.addAttribute("auctionDetails", details);
            return "admin-auction-detail";
        } catch (Exception e) {
            return "redirect:/admin/auctions?error=Auction not found";
        }
    }

    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String viewReportsPage(Model model, HttpServletResponse response) {
        // --- FIX: Add Cache-Control headers ---
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        // ------------------------------------

        model.addAttribute("reports", reportService.getAllReports());
        return "admin-reports"; // Renders the new admin-reports.html page
    }

    // === ADDED: New announcements method ===
    @GetMapping("/admin/announcements")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String showAnnouncementsPage(Model model) {
        model.addAttribute("announcements", announcementService.getAllAnnouncements());
        model.addAttribute("targetAudiences", TargetAudience.values());
        return "admin-announcements";
    }

    @GetMapping("/my-bids")
    @PreAuthorize("hasRole('BUYER')")
    public String myBidsPage(Authentication authentication, Model model) {
        List<MyBidDto> myBids = bidService.getBidsForBuyer(authentication.getName());
        model.addAttribute("bids", myBids);
        return "my-bids";
    }
}

// Custom exception for consistency with AuctionService
class AuctionNotFoundException extends RuntimeException {
    public AuctionNotFoundException(String message) {
        super(message);
    }
}
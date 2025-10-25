package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.dto.SystemReportDto;
import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.AuctionReport;
import com.bidwave.onlineauctionhub.models.Bid;
import com.bidwave.onlineauctionhub.models.User;
import com.bidwave.onlineauctionhub.repositories.AuctionReportRepository;
import com.bidwave.onlineauctionhub.repositories.AuctionRepository;
import com.bidwave.onlineauctionhub.repositories.BidRepository;
import com.bidwave.onlineauctionhub.repositories.UserRepository;
import com.bidwave.onlineauctionhub.service.reporting.ReportGenerationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final AuctionReportRepository reportRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final List<ReportGenerationStrategy> reportStrategies;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ReportService(AuctionReportRepository reportRepository, AuctionRepository auctionRepository,
                         BidRepository bidRepository, UserRepository userRepository,
                         List<ReportGenerationStrategy> reportStrategies,
                         SimpMessagingTemplate messagingTemplate) {
        this.reportRepository = reportRepository;
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
        this.reportStrategies = reportStrategies;
        this.messagingTemplate = messagingTemplate;
    }

    public AuctionReport createAuctionReport(Long auctionId, String adminEmail) {
        // Prevent duplicate reports
        if (reportRepository.existsByAuction_AuctionId(auctionId)) {
            throw new IllegalStateException("A report for this auction has already been generated.");
        }

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Bid winningBid = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction)
                .orElseThrow(() -> new IllegalStateException("Cannot generate report: No winning bid found for this auction."));

        AuctionReport report = new AuctionReport();
        report.setAuction(auction);
        report.setWinner(winningBid.getBuyer());
        report.setFinalBidAmount(winningBid.getBidAmount());
        report.setCreatedBy(admin);
        report.setGeneratedDate(LocalDateTime.now());

        // Broadcast dashboard update after creating a new report
        broadcastDashboardUpdate();

        return reportRepository.save(report);
    }

    public List<AuctionReport> getAllReports() {
        return reportRepository.findAllByOrderByGeneratedDateDesc();
    }

    @Transactional(readOnly = true)
    public SystemReportDto generateSystemActivityReport() {
        // 1. Get total user count
        long totalUsers = userRepository.count();

        // 2. Get total auction count
        long totalAuctions = auctionRepository.count();

        // 3. Get auction counts by status
        List<Object[]> statusCounts = auctionRepository.countAuctionsByStatus();
        Map<String, Long> auctionsByStatus = statusCounts.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));

        // 4. Assemble and return the DTO
        return new SystemReportDto(totalUsers, totalAuctions, auctionsByStatus);
    }

    /**
     * Generates a new system activity report and broadcasts it over WebSockets
     * to the admin dashboard topic.
     */
    @Transactional(readOnly = true)
    public void broadcastDashboardUpdate() {
        try {
            SystemReportDto report = generateSystemActivityReport();
            logger.info("Broadcasting real-time dashboard update: {} users, {} auctions", report.totalUsers(), report.totalAuctions());
            messagingTemplate.convertAndSend("/topic/admin/dashboard", report);
        } catch (Exception e) {
            logger.error("Error broadcasting dashboard update", e);
        }
    }

    public ByteArrayInputStream generateSystemActivityReport(String format) {
        ReportGenerationStrategy strategy = reportStrategies.stream()
                .filter(s -> s.getFormat().equalsIgnoreCase(format))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported report format: " + format));

        // Generate the system activity data and convert to list of strings for reporting
        SystemReportDto systemData = generateSystemActivityReport();
        List<String> reportData = convertSystemReportToLines(systemData);

        return strategy.generate(reportData);
    }

    public ByteArrayInputStream generateAuctionReportsExport(String format) {
        ReportGenerationStrategy strategy = reportStrategies.stream()
                .filter(s -> s.getFormat().equalsIgnoreCase(format))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported report format: " + format));

        List<AuctionReport> auctionReports = getAllReports();
        List<String> reportData = convertAuctionReportsToLines(auctionReports);

        return strategy.generate(reportData);
    }

    private List<String> convertSystemReportToLines(SystemReportDto systemData) {
        return List.of(
                "SYSTEM ACTIVITY REPORT",
                "======================",
                "Total Users: " + systemData.totalUsers(),
                "Total Auctions: " + systemData.totalAuctions(),
                "",
                "Auctions by Status:",
                systemData.auctionsByStatus().entrySet().stream()
                        .map(entry -> "  " + entry.getKey() + ": " + entry.getValue())
                        .collect(Collectors.joining("\n")),
                "",
                "Generated on: " + LocalDateTime.now()
        );
    }

    private List<String> convertAuctionReportsToLines(List<AuctionReport> auctionReports) {
        List<String> lines = new java.util.ArrayList<>();
        lines.add("AUCTION REPORTS EXPORT");
        lines.add("=====================");
        lines.add("");

        for (AuctionReport report : auctionReports) {
            lines.add("Report ID: " + report.getReportId());
            lines.add("Auction: " + report.getAuction().getItemName());
            lines.add("Winner: " + getUserDisplayName(report.getWinner()));
            lines.add("Final Bid Amount: $" + report.getFinalBidAmount());
            lines.add("Generated By: " + getUserDisplayName(report.getCreatedBy()));
            lines.add("Generated Date: " + report.getGeneratedDate());
            lines.add("---------------------");
        }

        return lines;
    }

    private String getUserDisplayName(User user) {
        if (user == null) {
            return "Unknown";
        }
        return user.getFirstName() + " " + user.getLastName();
    }
}
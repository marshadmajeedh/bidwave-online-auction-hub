package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.models.Auction;
import com.bidwave.onlineauctionhub.models.Bid;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class PdfGenerationService {

    // --- METHOD 1: Winner Report ---
    public ByteArrayInputStream generateWinnerReportPdf(Auction auction, Optional<Bid> winningBidOpt) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
            Paragraph title = new Paragraph("Auction Winner Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Auction Details Table
            PdfPTable auctionTable = new PdfPTable(2);
            auctionTable.setWidthPercentage(100);
            addHeaderCell(auctionTable, "Auction Details", 2);
            auctionTable.addCell("Auction ID:");
            auctionTable.addCell(String.valueOf(auction.getAuctionId()));
            auctionTable.addCell("Item Name:");
            auctionTable.addCell(auction.getItemName());
            auctionTable.addCell("Seller:");
            auctionTable.addCell(auction.getSeller().getFirstName() + " " + auction.getSeller().getLastName());
            auctionTable.addCell("End Time:");
            auctionTable.addCell(auction.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            document.add(auctionTable);
            document.add(Chunk.NEWLINE);

            // Winner Details Table
            if (winningBidOpt.isPresent()) {
                Bid winningBid = winningBidOpt.get();
                PdfPTable winnerTable = new PdfPTable(2);
                winnerTable.setWidthPercentage(100);
                addHeaderCell(winnerTable, "Winner Details", 2);
                winnerTable.addCell("Winner Name:");
                winnerTable.addCell(winningBid.getBuyer().getFirstName() + " " + winningBid.getBuyer().getLastName());
                winnerTable.addCell("Winner Email:");
                winnerTable.addCell(winningBid.getBuyer().getEmail());
                winnerTable.addCell("Winning Bid Amount:");
                winnerTable.addCell("$" + winningBid.getBidAmount().toString());
                winnerTable.addCell("Bid Time:");
                winnerTable.addCell(winningBid.getBidTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                document.add(winnerTable);
            } else {
                Paragraph noWinner = new Paragraph("This auction closed with no winning bids.");
                document.add(noWinner);
            }

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }


    // --- METHOD 2: Auction Bidders Report (Seller’s Report) ---
    public ByteArrayInputStream generateAuctionBiddersReportPdf(Auction auction, List<Bid> bids) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
            Paragraph title = new Paragraph("Auction Bid History Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Auction Details Table
            PdfPTable auctionTable = new PdfPTable(2);
            auctionTable.setWidthPercentage(100);
            auctionTable.setSpacingAfter(20);
            addHeaderCell(auctionTable, "Auction Details", 2);
            auctionTable.addCell("Item Name:");
            auctionTable.addCell(auction.getItemName());
            auctionTable.addCell("Auction Status:");
            auctionTable.addCell(auction.getStatus());
            document.add(auctionTable);

            // Bidders List Table
            PdfPTable biddersTable = new PdfPTable(3);
            biddersTable.setWidthPercentage(100);
            addHeaderCell(biddersTable, "Bid History", 3);

            // Add table headers
            biddersTable.addCell("Bidder Name");
            biddersTable.addCell("Bid Amount");
            biddersTable.addCell("Time of Bid");

            // Add bid data
            for (Bid bid : bids) {
                biddersTable.addCell(bid.getBuyer().getFirstName() + " " + bid.getBuyer().getLastName());
                biddersTable.addCell("$" + bid.getBidAmount().toString());
                biddersTable.addCell(bid.getBidTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            document.add(biddersTable);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }


    // --- SHARED HELPER ---
    private void addHeaderCell(PdfPTable table, String headerText, int colspan) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(new Color(34, 51, 59)); // dark-blue
        header.setPhrase(new Phrase(headerText, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.WHITE)));
        header.setColspan(colspan);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setPadding(10);
        table.addCell(header);
    }
}

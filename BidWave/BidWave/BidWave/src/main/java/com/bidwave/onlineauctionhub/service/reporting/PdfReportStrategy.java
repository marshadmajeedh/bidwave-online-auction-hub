package com.bidwave.onlineauctionhub.service.reporting;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Component
public class PdfReportStrategy implements ReportGenerationStrategy {
    @Override
    public ByteArrayInputStream generate(List<?> data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("System Activity Report (PDF)"));
            data.forEach(item -> document.add(new Paragraph(item.toString())));
            document.close();
        } catch (Exception e) { /* handle exception */ }
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public String getFormat() {
        return "PDF";
    }
}
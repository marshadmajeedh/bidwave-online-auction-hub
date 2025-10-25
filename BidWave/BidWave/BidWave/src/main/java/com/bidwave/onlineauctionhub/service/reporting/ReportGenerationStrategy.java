package com.bidwave.onlineauctionhub.service.reporting;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface ReportGenerationStrategy {
    ByteArrayInputStream generate(List<?> data);
    String getFormat();
}
package com.bidwave.onlineauctionhub.dto;

import java.math.BigDecimal;
import java.util.List;

public record AuctionChartDataDto(
        List<String> labels,
        List<BigDecimal> data
) {}
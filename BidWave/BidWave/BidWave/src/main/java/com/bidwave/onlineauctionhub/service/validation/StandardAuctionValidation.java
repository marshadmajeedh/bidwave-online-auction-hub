package com.bidwave.onlineauctionhub.service.validation;

import com.bidwave.onlineauctionhub.dto.AuctionCreateRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Component
public class StandardAuctionValidation implements AuctionValidationStrategy {
    @Override
    public void validate(AuctionCreateRequest request, MultipartFile[] imageFiles) {
        if (request.itemName() == null || request.itemName().isBlank()) {
            throw new IllegalArgumentException("Item name is required.");
        }
        if (request.startPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Starting price must be greater than zero.");
        }
        if (imageFiles == null || imageFiles.length == 0 || imageFiles[0].isEmpty()) {
            throw new IllegalArgumentException("At least one image is required.");
        }
    }
}
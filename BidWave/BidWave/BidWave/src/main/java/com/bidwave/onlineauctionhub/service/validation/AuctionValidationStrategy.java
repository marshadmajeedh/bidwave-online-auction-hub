package com.bidwave.onlineauctionhub.service.validation;

import com.bidwave.onlineauctionhub.dto.AuctionCreateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AuctionValidationStrategy {
    void validate(AuctionCreateRequest request, MultipartFile[] imageFiles);
}
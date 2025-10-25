package com.bidwave.onlineauctionhub.repositories;

import com.bidwave.onlineauctionhub.models.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerRepository extends JpaRepository<Buyer, Long> {
}
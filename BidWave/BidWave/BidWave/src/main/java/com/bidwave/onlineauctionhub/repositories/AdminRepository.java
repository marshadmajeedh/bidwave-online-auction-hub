package com.bidwave.onlineauctionhub.repositories;

import com.bidwave.onlineauctionhub.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}
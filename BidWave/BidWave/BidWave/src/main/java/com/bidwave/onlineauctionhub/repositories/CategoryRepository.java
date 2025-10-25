package com.bidwave.onlineauctionhub.repositories;

import com.bidwave.onlineauctionhub.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
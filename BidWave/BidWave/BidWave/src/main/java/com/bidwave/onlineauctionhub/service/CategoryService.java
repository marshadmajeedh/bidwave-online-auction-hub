package com.bidwave.onlineauctionhub.service;

import com.bidwave.onlineauctionhub.models.Category;
import com.bidwave.onlineauctionhub.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}

package com.bidwave.onlineauctionhub;

import com.bidwave.onlineauctionhub.models.Admin;
import com.bidwave.onlineauctionhub.models.Category;
import com.bidwave.onlineauctionhub.repositories.CategoryRepository;
import com.bidwave.onlineauctionhub.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(CategoryRepository categoryRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed categories if the table is empty
        if (categoryRepository.count() == 0) {
            seedCategories();
        }

        // Seed a default admin user if one doesn't exist
        if (userRepository.findByEmail("admin@bidwave.com").isEmpty()) {
            createDefaultAdmin();
        }
    }

    private void seedCategories() {
        List<String> categoryNames = Arrays.asList(
                // Existing Categories
                "Electronics", "Furniture", "Vehicles", "Antiques & Collectibles", "Clothing & Apparel",
                "Jewelry & Watches", "Home & Garden", "Books, Movies & Music", "Sporting Goods", "Toys & Hobbies",
                "Art & Crafts", "Health & Beauty", "Real Estate", "Computers & Tablets", "Mobile Phones",
                "Cameras & Photo", "Musical Instruments", "Business & Industrial", "Pottery & Glass", "Coins & Paper Money",

                // --- NEW CATEGORIES ADDED ---
                "Action Figures",
                "Trading Cards",
                "Stamps",
                "Video Games & Consoles",
                "Home Decor",
                "Kitchenware & Appliances",
                "Handbags & Accessories",
                "Sneakers",
                "Bedding & Bath",
                "Baby Gear",
                "Pet Supplies",
                "Event Tickets",
                "Gift Cards & Coupons",
                "Vinyl Records",
                "Scientific & Medical Antiques"
                // --- END OF NEW CATEGORIES ---
        );

        List<Category> categories = categoryNames.stream().map(name -> {
            Category category = new Category();
            category.setName(name);
            return category;
        }).collect(Collectors.toList());

        categoryRepository.saveAll(categories);
        System.out.println(">>>> " + categoryNames.size() + " categories have been seeded to the database.");
    }

    private void createDefaultAdmin() {
        Admin admin = new Admin();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@bidwave.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setStatus("ACTIVE");
        admin.setEnabled(true);
        admin.setRegistrationDate(LocalDateTime.now());
        userRepository.save(admin);
        System.out.println(">>>> Default admin user created. Email: admin@bidwave.com | Password: Admin@123");
    }
}
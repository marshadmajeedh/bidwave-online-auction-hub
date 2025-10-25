-- Drop tables in the correct order to respect foreign key constraints
-- Drop child tables first, then parent tables

DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `verification_token`;
DROP TABLE IF EXISTS `bids`;
DROP TABLE IF EXISTS `auction_categories`;
DROP TABLE IF EXISTS `auction_images`;
DROP TABLE IF EXISTS `auction_reports`;  -- Moved BEFORE auctions since it references auctions
DROP TABLE IF EXISTS `auctions`;         -- Now can be dropped after auction_reports
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `announcements`;
DROP TABLE IF EXISTS `users`;

-- Create the users table
CREATE TABLE `users` (
                         `user_id` BIGINT NOT NULL AUTO_INCREMENT,
                         `user_role` VARCHAR(31) NOT NULL,
                         `email` VARCHAR(255) NOT NULL UNIQUE,
                         `enabled` BIT(1) NOT NULL,
                         `first_name` VARCHAR(255),
                         `last_name` VARCHAR(255),
                         `last_login` DATETIME(6),
                         `password` VARCHAR(255) NOT NULL,
                         `registration_date` DATETIME(6),
                         `status` VARCHAR(255),
                         `billing_address` VARCHAR(255),
                         `business_reg_number` VARCHAR(255),
                         `id_verification_status` VARCHAR(255),
                         `phone_number` VARCHAR(255),
                         `shipping_address` VARCHAR(255),
                         PRIMARY KEY (`user_id`)
) ENGINE=InnoDB;

-- Create the categories table
CREATE TABLE `categories` (
                              `category_id` BIGINT NOT NULL AUTO_INCREMENT,
                              `name` VARCHAR(255) NOT NULL UNIQUE,
                              PRIMARY KEY (`category_id`)
) ENGINE=InnoDB;

-- Create the auctions table
CREATE TABLE `auctions` (
                            `auction_id` BIGINT NOT NULL AUTO_INCREMENT,
                            `description` LONGTEXT,
                            `end_time` DATETIME(6),
                            `item_name` VARCHAR(255) NOT NULL,
                            `start_price` DECIMAL(19,2) NOT NULL,
                            `status` VARCHAR(255),
                            `auction_type` VARCHAR(255) NOT NULL DEFAULT 'STANDARD', -- <-- ADD THIS LINE
                            `seller_user_id` BIGINT NOT NULL,
                            PRIMARY KEY (`auction_id`),
                            FOREIGN KEY (`seller_user_id`) REFERENCES `users`(`user_id`)
) ENGINE=InnoDB;

-- Create the auction_images table
CREATE TABLE `auction_images` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                                  `image_url` VARCHAR(255) NOT NULL,
                                  `auction_id` BIGINT NOT NULL,
                                  PRIMARY KEY (`id`),
                                  FOREIGN KEY (`auction_id`) REFERENCES `auctions`(`auction_id`)
) ENGINE=InnoDB;

-- Create the auction_categories join table
CREATE TABLE `auction_categories` (
                                      `auction_id` BIGINT NOT NULL,
                                      `category_id` BIGINT NOT NULL,
                                      PRIMARY KEY (`auction_id`, `category_id`),
                                      FOREIGN KEY (`auction_id`) REFERENCES `auctions`(`auction_id`),
                                      FOREIGN KEY (`category_id`) REFERENCES `categories`(`category_id`)
) ENGINE=InnoDB;

-- Create the bids table
CREATE TABLE `bids` (
                        `bid_id` BIGINT NOT NULL AUTO_INCREMENT,
                        `bid_amount` DECIMAL(19,2) NOT NULL,
                        `bid_time` DATETIME(6) NOT NULL,
                        `auction_id` BIGINT NOT NULL,
                        `buyer_user_id` BIGINT NOT NULL,
                        PRIMARY KEY (`bid_id`),
                        FOREIGN KEY (`auction_id`) REFERENCES `auctions`(`auction_id`),
                        FOREIGN KEY (`buyer_user_id`) REFERENCES `users`(`user_id`)
) ENGINE=InnoDB;

-- Create the verification_token table
CREATE TABLE `verification_token` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT,
                                      `expiry_date` DATETIME(6),
                                      `token` VARCHAR(255),
                                      `user_id` BIGINT NOT NULL UNIQUE,
                                      PRIMARY KEY (`id`),
                                      FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`)
) ENGINE=InnoDB;

-- Create the notifications table
CREATE TABLE `notifications` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `created_at` DATETIME(6),
                                 `is_read` BIT(1) NOT NULL,
                                 `message` VARCHAR(255) NOT NULL,
                                 `user_id` BIGINT NOT NULL,
                                 PRIMARY KEY (`id`),
                                 FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`)
) ENGINE=InnoDB;

-- Create the auction_reports table
CREATE TABLE `auction_reports` (
                                   `report_id` BIGINT NOT NULL AUTO_INCREMENT,
                                   `final_bid_amount` DECIMAL(19,2),
                                   `generated_date` DATETIME(6),
                                   `auction_id` BIGINT NOT NULL,
                                   `created_by_admin_id` BIGINT,
                                   `winner_user_id` BIGINT,
                                   PRIMARY KEY (`report_id`),
                                   FOREIGN KEY (`auction_id`) REFERENCES `auctions`(`auction_id`),
                                   FOREIGN KEY (`created_by_admin_id`) REFERENCES `users`(`user_id`),
                                   FOREIGN KEY (`winner_user_id`) REFERENCES `users`(`user_id`)
) ENGINE=InnoDB;

-- === ADD THIS NEW TABLE CREATION SCRIPT ===
CREATE TABLE `announcements` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `content` LONGTEXT NOT NULL,
                                 `target_audience` VARCHAR(20) NOT NULL,
                                 `created_at` DATETIME(6) NOT NULL,
                                 `admin_user_id` BIGINT NOT NULL,
                                 PRIMARY KEY (`id`),
                                 FOREIGN KEY (`admin_user_id`) REFERENCES `users`(`user_id`)
) ENGINE=InnoDB;
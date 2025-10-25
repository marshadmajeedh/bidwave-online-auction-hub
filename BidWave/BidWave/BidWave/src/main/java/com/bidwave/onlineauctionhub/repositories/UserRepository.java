package com.bidwave.onlineauctionhub.repositories;

import com.bidwave.onlineauctionhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Custom query to find a user by their email address
    Optional<User> findByEmail(String email);

    /**
     * Finds users based on their specific entity type (role).
     * @param role The class of the user type to find (e.g., Buyer.class, Seller.class)
     * @return A list of users of that specific type.
     */
    @Query("SELECT u FROM User u WHERE TYPE(u) = :role")
    List<User> findAllByUserRole(@Param("role") Class<? extends User> role);
}
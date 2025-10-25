package com.bidwave.onlineauctionhub.repositories;

import com.bidwave.onlineauctionhub.models.User;
import com.bidwave.onlineauctionhub.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);

    // --- ADD THIS NEW METHOD ---
    /**
     * Deletes a verification token associated with a specific user.
     * This is used when deleting a user account.
     * @param user The user whose token should be deleted.
     */
    @Transactional
    void deleteByUser(User user);
}
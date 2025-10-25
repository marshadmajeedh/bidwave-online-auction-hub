package com.bidwave.onlineauctionhub.repositories;

import com.bidwave.onlineauctionhub.models.Notification;
import com.bidwave.onlineauctionhub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    long countByUserAndIsReadFalse(User user);

    Optional<Notification> findByIdAndUser(Long id, User user);

    List<Notification> findByUserAndIsReadFalse(User user);

    // --- ADD THIS NEW METHOD ---
    /**
     * Deletes all notifications associated with a specific user.
     * This is used when deleting a user account.
     * @param user The user whose notifications should be deleted.
     */
    @Transactional
    void deleteAllByUser(User user);
}
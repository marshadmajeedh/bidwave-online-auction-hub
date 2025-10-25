package com.bidwave.onlineauctionhub.repositories;

import com.bidwave.onlineauctionhub.models.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // Find all announcements, with the newest one first
    List<Announcement> findAllByOrderByCreatedAtDesc();
}

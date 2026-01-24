package com.rathaur.nexus.statsservice.repository;

import com.rathaur.nexus.statsservice.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/23/2026
 */
public interface UserStatsRepository extends JpaRepository<UserStats, String> {

    Optional<UserStats> findByUsername(String username);

    // Fast Leaderboard: Top 10 users by Nexus Score
    List<UserStats> findTop10ByOrderByNexusScoreDesc();

    /**
     * ADVANCED JSONB QUERY:
     * Find users who have a specific badge in their "quickSummary" JSON blob.
     * Uses PostgreSQL native syntax: ->> (extract text)
     */
    @Query(value = "SELECT * FROM user_stats u WHERE u.quick_summary ->> 'current_status' = 'OPEN_TO_WORK'",
            nativeQuery = true)
    List<UserStats> findUsersOpenToWork();

}

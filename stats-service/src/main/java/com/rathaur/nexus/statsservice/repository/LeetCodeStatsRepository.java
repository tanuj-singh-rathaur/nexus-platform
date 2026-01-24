package com.rathaur.nexus.statsservice.repository;

import com.rathaur.nexus.statsservice.entity.LeetCodeStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LeetCodeStatsRepository extends JpaRepository<LeetCodeStats, String> {
    Optional<LeetCodeStats> findByUsername(String username);
}
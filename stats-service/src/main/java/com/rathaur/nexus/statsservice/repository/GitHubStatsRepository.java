package com.rathaur.nexus.statsservice.repository;

import com.rathaur.nexus.statsservice.entity.GitHubStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GitHubStatsRepository extends JpaRepository<GitHubStats, String> {

    Optional<GitHubStats> findByUsername(String username);

    /**
     * FUTURISTIC QUERY:
     * Digs inside the 'telemetry' JSON blob to find users whose top language is Java.
     * This proves you don't need a column for everything!
     */
    @Query(value = "SELECT * FROM github_stats g WHERE g.telemetry -> 'top_languages' ->> 0 LIKE '%Java%'",
            nativeQuery = true)
    List<GitHubStats> findJavaExperts();
}
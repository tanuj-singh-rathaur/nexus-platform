package com.rathaur.nexus.statsservice.service;

import com.rathaur.nexus.statsservice.entity.GitHubStats;
import com.rathaur.nexus.statsservice.entity.LeetCodeStats;
import com.rathaur.nexus.statsservice.entity.UserStats;
import com.rathaur.nexus.statsservice.repository.GitHubStatsRepository;
import com.rathaur.nexus.statsservice.repository.LeetCodeStatsRepository;
import com.rathaur.nexus.statsservice.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/24/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsPersistenceService {

    private final UserStatsRepository userStatsRepo;
    private final GitHubStatsRepository gitHubStatsRepo;
    private final LeetCodeStatsRepository leetCodeStatsRepo;

    @Transactional
    public void ensureMasterExists(String username) {
        userStatsRepo.findById(username)
                .orElseGet(() -> userStatsRepo.save(
                        UserStats.builder().username(username).nexusScore(0.0).build()
                ));
    }

    @Transactional
    public void saveGithubAndRecalc(String username, Map<String, Object> data) {
        UserStats master = userStatsRepo.findById(username).orElseThrow();

        GitHubStats stats = gitHubStatsRepo.findById(username).orElse(null);

        if (stats == null) {
            stats = new GitHubStats();
            stats.setUserStats(master);   // MUST set first (MapsId will populate PK)
        } else {
            // already has PK, still keep association consistent
            stats.setUserStats(master);
        }

        Map<String, Object> user = (Map<String, Object>) data.get("user");
        Map<String, Object> repos = (Map<String, Object>) user.get("repositories");
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) repos.get("nodes");
        Map<String, Object> contributions = (Map<String, Object>) user.get("contributionsCollection");
        Map<String, Object> calendar = (Map<String, Object>) contributions.get("contributionCalendar");

        int stars = nodes.stream()
                .mapToInt(n -> n.get("stargazerCount") != null ? ((Number) n.get("stargazerCount")).intValue() : 0)
                .sum();

        stats.setPublicRepos(((Number) repos.get("totalCount")).intValue());
        stats.setTotalStars(stars);
        stats.setTotalCommitsYearly(((Number) calendar.get("totalContributions")).intValue());
        stats.setTelemetry(data);
        stats.setLastSynced(Instant.now());
        log.info("LC before save -> id={}, masterId={}", stats.getUsername(), master.getUsername());
        gitHubStatsRepo.saveAndFlush(stats);

        recalc(master);
    }

    @Transactional
    public void saveLeetCodeAndRecalc(String username, Map<String, Object> data) {
        UserStats master = userStatsRepo.findById(username).orElseThrow();

        LeetCodeStats stats = leetCodeStatsRepo.findById(username).orElse(null);

        if (stats == null) {
            stats = new LeetCodeStats();
            stats.setUserStats(master);
        } else {
            stats.setUserStats(master);
        }

        // 1. Path Walking
        Map<String, Object> matchedUser = (Map<String, Object>) data.get("matchedUser");
        Map<String, Object> profile = (Map<String, Object>) matchedUser.get("profile");
        List<Map<String, Object>> submissionStats = (List<Map<String, Object>>)
                ((Map<String, Object>) matchedUser.get("submitStats")).get("acSubmissionNum");

        // 2. Map all difficulty levels (0=All, 1=Easy, 2=Medium, 3=Hard)
        stats.setGlobalRanking(((Number) profile.get("ranking")).intValue());

        // Using defensive checks in case the list order ever changes
        stats.setTotalSolved(((Number) submissionStats.get(0).get("count")).intValue());
        stats.setEasySolved(((Number) submissionStats.get(1).get("count")).intValue());
        stats.setMediumSolved(((Number) submissionStats.get(2).get("count")).intValue());
        stats.setHardSolved(((Number) submissionStats.get(3).get("count")).intValue());

        stats.setAdvancedMetrics(data);
        stats.setLastSynced(Instant.now());

        log.info("Saving LeetCode stats for: {}", username);
        leetCodeStatsRepo.saveAndFlush(stats);

        recalc(master);
    }

    private void recalc(UserStats master) {
        String username = master.getUsername();
        GitHubStats gh = gitHubStatsRepo.findById(username).orElse(null);
        LeetCodeStats lc = leetCodeStatsRepo.findById(username).orElse(null);

        double score = 0;
        if (gh != null) score += (gh.getTotalStars() * 10) + (gh.getPublicRepos() * 2);
        if (lc != null) score += (lc.getTotalSolved() != null ? lc.getTotalSolved() * 5 : 0)
                + (lc.getHardSolved() != null ? lc.getHardSolved() * 20 : 0);

        master.setNexusScore(score);
        userStatsRepo.save(master);
        master.setLastSyncAll(Instant.now());
        log.info("Nexus Score updated for {}: {}", username, score);
    }
}

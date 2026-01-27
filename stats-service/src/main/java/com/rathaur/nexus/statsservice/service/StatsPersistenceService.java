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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        GitHubStats stats = gitHubStatsRepo.findById(username).orElseGet(() -> {
            GitHubStats s = new GitHubStats();
            s.setUserStats(master);
            return s;
        });

        Map<String, Object> user = (Map<String, Object>) data.get("user");
        if (user == null) return;

        try {
            // 1. Extract TOTAL COUNT from the alias
            Map<String, Object> allRepos = (Map<String, Object>) user.getOrDefault("allRepos", Collections.emptyMap());
            int totalReposCount = extractInt(allRepos, "totalCount");

            // 2. Extract NODES from the other alias
            Map<String, Object> recentRepos = (Map<String, Object>) user.getOrDefault("recentRepos", Collections.emptyMap());
            List<Map<String, Object>> repoNodes = (List<Map<String, Object>>) recentRepos.getOrDefault("nodes", Collections.emptyList());

            Map<String, Object> collection = (Map<String, Object>) user.getOrDefault("contributionsCollection", Collections.emptyMap());
            Map<String, Object> calendar = (Map<String, Object>) collection.getOrDefault("contributionCalendar", Collections.emptyMap());

            // 3. Calculate stars
            int stars = repoNodes.stream()
                    .filter(Objects::nonNull)
                    .mapToInt(n -> {
                        Object s = n.get("stargazerCount");
                        return (s instanceof Number num) ? num.intValue() : 0;
                    }).sum();

            // 4. Set Values - totalReposCount will now be 16
            stats.setPublicRepos(totalReposCount);
            stats.setTotalStars(stars);
            stats.setTotalCommitsYearly(extractInt(calendar, "totalContributions"));

            stats.setTelemetry(data);
            stats.setLastSynced(Instant.now());

            gitHubStatsRepo.saveAndFlush(stats);
            recalc(master);

        } catch (Exception e) {
            log.error("NEXUS-PERSIST: Mapping failed", e);
        }
    }
    private int extractInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return (val instanceof Number n) ? n.intValue() : 0;
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
        GitHubStats gh = master.getGithubStats();
        LeetCodeStats lc = master.getLeetCodeStats();

        double score = 0;
        if (gh != null) {
            score += (gh.getPublicRepos() * 25);
            score += (gh.getTotalStars() * 10);
            score += (gh.getTotalCommitsYearly() * 5);
        }

        if (lc != null) {
            score += (lc.getEasySolved() != null ? lc.getEasySolved() * 2 : 0);
            score += (lc.getMediumSolved() != null ? lc.getMediumSolved() * 15 : 0);
            score += (lc.getHardSolved() != null ? lc.getHardSolved() * 50 : 0);
        }

        master.setNexusScore(score);

        // --- FIX: Wrap the String in a Map to match the Entity's required type ---
        if (gh != null) {
            String bioMessage = String.format(
                    "Fullstack developer with %d projects and %d commits this week. LeetCode: %d solved.",
                    gh.getPublicRepos(),
                    gh.getTotalCommitsYearly(),
                    (lc != null ? lc.getTotalSolved() : 0)
            );

            // Setting it as a Map so Hibernate can persist it as JSON
            master.setQuickSummary(Map.of(
                    "headline", bioMessage,
                    "lastUpdated", Instant.now().toString()
            ));
        }

        userStatsRepo.save(master);
    }
}
package com.rathaur.nexus.statsservice.service;

import com.rathaur.nexus.statsservice.entity.*;
import com.rathaur.nexus.statsservice.exception.StatsDomainExceptions.DataParsingException;
import com.rathaur.nexus.statsservice.exception.StatsDomainExceptions.SyncServiceException;
import com.rathaur.nexus.statsservice.repository.*;
import io.micrometer.observation.annotation.Observed;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Persistence service for Nexus Platform Stats.
 * Handles defensive data parsing, score recalculation, and semantic logging.
 * * @author Tanuj Singh Rathaur
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Observed(name = "stats.persistence.service")
public class StatsPersistenceService {

    private final UserStatsRepository userStatsRepo;
    private final GitHubStatsRepository gitHubStatsRepo;
    private final LeetCodeStatsRepository leetCodeStatsRepo;

    @Transactional
    public void ensureMasterExists(String username) {
        log.debug("Checking master record existence for user: {}", username);
        userStatsRepo.findById(username)
                .orElseGet(() -> {
                    log.info("Creating new UserStats master record for: {}", username);
                    return userStatsRepo.save(UserStats.builder()
                            .username(username)
                            .nexusScore(0.0)
                            .build());
                });
    }

    @Transactional
    public void saveGithubAndRecalc(String username, Map<String, Object> data) {
        log.info("Persisting GitHub telemetry for user: {}", username);

        UserStats master = userStatsRepo.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("Master record missing for: " + username));

        try {
            GitHubStats stats = gitHubStatsRepo.findById(username).orElse(new GitHubStats());
            stats.setUserStats(master);

            // --- Defensive Path Walking ---
            Map<String, Object> user = navigateMap(data, "user");
            Map<String, Object> repos = navigateMap(user, "repositories");
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) repos.get("nodes");
            Map<String, Object> contributions = navigateMap(user, "contributionsCollection");
            Map<String, Object> calendar = navigateMap(contributions, "contributionCalendar");

            // Ensure you are navigating into the "user" -> "repositories" -> "nodes" correctly
            int stars = (nodes == null) ? 0 : nodes.stream()
                    .filter(Objects::nonNull)
                    .mapToInt(n -> {
                        Object count = n.get("stargazerCount");
                        // Log this to see what GitHub is actually returning
                        log.debug("User: {} | Repo Star Count: {}", username, count);
                        return extractInt(count);
                    })
                    .sum();

            stats.setPublicRepos(extractInt(repos.get("totalCount")));
            stats.setTotalStars(stars);
            stats.setTotalCommitsYearly(extractInt(calendar.get("totalContributions")));
            stats.setTelemetry(data);
            stats.setLastSynced(Instant.now());

            gitHubStatsRepo.saveAndFlush(stats);
            log.debug("GitHub stats saved. Recalculating Nexus Score for {}", username);

            recalc(master);

        } catch (Exception e) {
            log.error("CRITICAL: Failed to parse/save GitHub data for user {}. Error: {}", username, e.getMessage());
            throw new DataParsingException("Error extracting GitHub telemetry fields");
        }
    }

    @Transactional
    public void saveLeetCodeAndRecalc(String username, Map<String, Object> data) {
        log.info("Persisting LeetCode telemetry for user: {}", username);

        UserStats master = userStatsRepo.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("Master record missing for: " + username));

        try {
            LeetCodeStats stats = leetCodeStatsRepo.findById(username).orElse(new LeetCodeStats());
            stats.setUserStats(master);

            Map<String, Object> matchedUser = navigateMap(data, "matchedUser");
            Map<String, Object> profile = navigateMap(matchedUser, "profile");
            Map<String, Object> submitStats = navigateMap(matchedUser, "submitStats");
            List<Map<String, Object>> submissionStats = (List<Map<String, Object>>) submitStats.get("acSubmissionNum");

            // --- NEXUS FIX: Graceful handling of empty stats ---
            if (submissionStats == null || submissionStats.isEmpty()) {
                log.warn("NEXUS-STATS: LeetCode stats empty for {}. Defaulting to zeros.", username);
                stats.setTotalSolved(0);
                stats.setEasySolved(0);
                stats.setMediumSolved(0);
                stats.setHardSolved(0);
            } else {
                stats.setTotalSolved(extractInt(submissionStats.get(0).get("count")));
                stats.setEasySolved(extractInt(submissionStats.get(1).get("count")));
                stats.setMediumSolved(extractInt(submissionStats.get(2).get("count")));
                stats.setHardSolved(extractInt(submissionStats.get(3).get("count")));
            }

            stats.setGlobalRanking(extractInt(profile.get("ranking")));
            stats.setAdvancedMetrics(data);
            stats.setLastSynced(Instant.now());

            leetCodeStatsRepo.saveAndFlush(stats);
            recalc(master);

        } catch (Exception e) {
            // Log the error but don't necessarily crash the whole sync if it's just a parsing glitch
            log.error("NEXUS-STATS: Non-fatal error parsing LeetCode for {}: {}", username, e.getMessage());
            // Optionally: throw a custom NON-ROLLBACK exception if you want to notify the caller
        }
    }

    private void recalc(UserStats master) {
        String username = master.getUsername();
        GitHubStats gh = gitHubStatsRepo.findById(username).orElse(null);
        LeetCodeStats lc = leetCodeStatsRepo.findById(username).orElse(null);

        double oldScore = master.getNexusScore();
        double score = 0;

        if (gh != null) {
            score += (gh.getTotalStars() * 10) + (gh.getPublicRepos() * 2);
        }
        if (lc != null) {
            score += (Optional.ofNullable(lc.getTotalSolved()).orElse(0) * 5)
                    + (Optional.ofNullable(lc.getHardSolved()).orElse(0) * 20);
        }

        master.setNexusScore(score);
        master.setLastSyncAll(Instant.now());
        userStatsRepo.save(master);

        log.info("SCORE_UPDATE: User={} | Old={} | New={} | Delta={}",
                username, oldScore, score, (score - oldScore));
    }

    // --- Helper Utilities ---

    private Map<String, Object> navigateMap(Object obj, String fieldName) {
        if (obj instanceof Map) {
            Object target = ((Map<?, ?>) obj).get(fieldName);
            if (target instanceof Map) {
                return (Map<String, Object>) target;
            }
        }
        log.warn("NEXUS-STATS: Navigation failed for field: {}. Returning empty map.", fieldName);
        return Map.of(); // Return empty map instead of throwing exception
    }

    private int extractInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return 0;
    }
}
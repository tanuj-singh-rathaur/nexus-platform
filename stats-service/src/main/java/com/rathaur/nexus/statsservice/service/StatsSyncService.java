package com.rathaur.nexus.statsservice.service;

import com.rathaur.nexus.statsservice.client.GitHubClient;
import com.rathaur.nexus.statsservice.client.LeetCodeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Orchestrates the synchronization of developer stats.
 * Synchronous version for deep debugging and stability.
 *
 * @author Tanuj Singh Rathaur
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsSyncService {

    private final GitHubClient gitHubClient;
    private final LeetCodeClient leetCodeClient;
    private final StatsPersistenceService persistence;

    public void fullStatsSync(String username, String githubHandle, String leetcodeHandle) {
        log.info("NEXUS-SYNC: Starting [Synchronous] sync for user: {}", username);

        // 1. Ensure master record exists
        try {
            persistence.ensureMasterExists(username);
            log.info("NEXUS-SYNC: Master record confirmed for {}", username);
        } catch (Exception e) {
            log.error("NEXUS-SYNC: Master check failed: {}", e.getMessage(), e);
            return; // Stop if we can't establish a master record
        }

        // 2. Process GitHub Sync
        if (githubHandle != null && !githubHandle.isBlank()) {
            try {
                log.info("NEXUS-SYNC: Initiating GitHub fetch for handle: {}", githubHandle);
                // Assuming GitHubClient.fetchUserStats now returns Map<String, Object>
                Map<String, Object> githubData = gitHubClient.fetchUserStats(githubHandle);

                log.info("NEXUS-SYNC: GitHub data received, persisting...");
                persistence.saveGithubAndRecalc(username, githubData);
                log.info("NEXUS-SYNC: GitHub sync completed successfully.");
            } catch (Exception e) {
                log.error("NEXUS-SYNC: GitHub sync failed for {}: {}", username, e.getMessage(), e);
            }
        } else {
            log.warn("NEXUS-SYNC: GitHub handle missing, skipping.");
        }

        // 3. Process LeetCode Sync
        if (leetcodeHandle != null && !leetcodeHandle.isBlank()) {
            try {
                log.info("NEXUS-SYNC: Initiating LeetCode fetch for handle: {}", leetcodeHandle);
                // Assuming LeetCodeClient.fetchUserStats now returns Map<String, Object>
                Map<String, Object> leetcodeData = leetCodeClient.fetchUserStats(leetcodeHandle);

                log.info("NEXUS-SYNC: LeetCode data received, persisting...");
                persistence.saveLeetCodeAndRecalc(username, leetcodeData);
                log.info("NEXUS-SYNC: LeetCode sync completed successfully.");
            } catch (Exception e) {
                log.error("NEXUS-SYNC: LeetCode sync failed for {}: {}", username, e.getMessage(), e);
            }
        } else {
            log.warn("NEXUS-SYNC: LeetCode handle missing, skipping.");
        }

        log.info("NEXUS-SYNC: Full sync process finished for user: {}", username);
    }
}
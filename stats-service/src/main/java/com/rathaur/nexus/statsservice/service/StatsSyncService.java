package com.rathaur.nexus.statsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rathaur.nexus.statsservice.client.GitHubClient;
import com.rathaur.nexus.statsservice.client.LeetCodeClient;
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
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/24/2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsSyncService {

    private final GitHubClient gitHubClient;
    private final LeetCodeClient leetCodeClient;
    private final StatsPersistenceService persistence;

    public void fullStatsSync(String username, String githubHandle, String leetcodeHandle) {
        log.info("SYNC-START: User={}, GitHub={}, LeetCode={}", username, githubHandle, leetcodeHandle);

        persistence.ensureMasterExists(username);

        if (githubHandle != null) {
            log.info("GITHUB-TRIGGER: Fetching stats for handle: {}", githubHandle);
            gitHubClient.fetchUserStats(githubHandle)
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(data -> {
                        log.info("GITHUB-DATA-RECEIVED: Data keys found: {}", data.keySet());
                        persistence.saveGithubAndRecalc(username, data);
                    })
                    .doOnError(e -> log.error("GITHUB-STREAM-ERROR for {}: {}", username, e.getMessage(), e))
                    .subscribe(
                            v -> log.info("GITHUB-SYNC-COMPLETE: User={}", username),
                            e -> log.error("GITHUB-SUBSCRIPTION-FATAL: {}", e.getMessage())
                    );
        }

        if (leetcodeHandle != null) {
            log.info("LEETCODE-TRIGGER: Fetching stats for handle: {}", leetcodeHandle);
            leetCodeClient.fetchUserStats(leetcodeHandle)
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(data -> {
                        log.info("LEETCODE-DATA-RECEIVED: Data keys found: {}", data.keySet());
                        persistence.saveLeetCodeAndRecalc(username, data);
                    })
                    .doOnError(e -> log.error("LEETCODE-STREAM-ERROR for {}: {}", username, e.getMessage(), e))
                    .subscribe(
                            v -> log.info("LEETCODE-SYNC-COMPLETE: User={}", username),
                            e -> log.error("LEETCODE-SUBSCRIPTION-FATAL: {}", e.getMessage())
                    );
        }
    }
}
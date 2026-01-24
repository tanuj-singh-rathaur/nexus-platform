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

        persistence.ensureMasterExists(username);

        if (githubHandle != null) {
            gitHubClient.fetchUserStats(githubHandle)
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(data -> persistence.saveGithubAndRecalc(username, data))
                    .doOnError(e -> log.error("GitHub API failed for {}: {}", username, e.getMessage()))
                    .subscribe(v -> {}, e -> log.error("SYNC FAILED", e));

        }

        if (leetcodeHandle != null) {
            leetCodeClient.fetchUserStats(leetcodeHandle)
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(data -> persistence.saveLeetCodeAndRecalc(username, data))
                    .doOnError(e -> log.error("LeetCode API failed for {}: {}", username, e.getMessage()))
                    .subscribe(v -> {}, e -> log.error("SYNC FAILED", e));

        }
    }
}

package com.rathaur.nexus.statsservice.controller;


import com.rathaur.nexus.common.dto.ApiResponse;
import com.rathaur.nexus.statsservice.entity.UserStats;
import com.rathaur.nexus.statsservice.repository.UserStatsRepository;
import com.rathaur.nexus.statsservice.service.StatsSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Stats Controller using Nexus Standard ApiResponse.
 * @author Tanuj Singh Rathaur
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsSyncService statsSyncService;
    private final UserStatsRepository userStatsRepository;

    /**
     * TRIGGER: Manually start a sync.
     * Returns 202 Accepted because the work happens via WebFlux in the background.
     */
    @PostMapping("/sync/{username}")
    public ResponseEntity<ApiResponse<String>> triggerSync(
            @PathVariable String username,
            @RequestParam(required = false) String github,
            @RequestParam(required = false) String leetcode) {

        String traceId = UUID.randomUUID().toString();
        log.info("[{}] Manual sync triggered for: {}", traceId, username);

        statsSyncService.fullStatsSync(username, github, leetcode);

        return ResponseEntity.accepted().body(
                ApiResponse.ok("Synchronization started for " + username, null, traceId)
        );
    }

    /**
     * QUERY: Get user statistics.
     */
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserStats>> getUserStats(@PathVariable String username) {
        String traceId = UUID.randomUUID().toString();

        return userStatsRepository.findByUsername(username)
                .map(stats -> ResponseEntity.ok(ApiResponse.ok("Stats retrieved", stats, traceId)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * LEADERBOARD: Fetch top 10 developers.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<UserStats>>> getLeaderboard() {
        String traceId = UUID.randomUUID().toString();
        List<UserStats> topUsers = userStatsRepository.findTop10ByOrderByNexusScoreDesc();

        return ResponseEntity.ok(ApiResponse.ok("Leaderboard retrieved", topUsers, traceId));
    }
}
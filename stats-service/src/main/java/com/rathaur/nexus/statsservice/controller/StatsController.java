package com.rathaur.nexus.statsservice.controller;

import com.rathaur.nexus.common.dto.ApiResponse;
import com.rathaur.nexus.statsservice.entity.UserStats;
import com.rathaur.nexus.statsservice.repository.UserStatsRepository;
import com.rathaur.nexus.statsservice.service.StatsSyncService;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller focusing on the "Service-to-Service" logic for the Nexus Platform.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Observed(name = "stats.controller")
public class StatsController {

    private final StatsSyncService statsSyncService;
    private final UserStatsRepository userStatsRepository;
    private final Tracer tracer;

    /**
     * SECURE SYNC: Syncs ONLY the logged-in user.
     * No username in URL - pulled from JWT 'sub' (Authentication.getName())
     */
    @PostMapping("/sync")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> syncMe(
            Authentication auth,
            @RequestParam(required = false) String github,
            @RequestParam(required = false) String leetcode) {

        String currentUsername = auth.getName();
        log.info("Secure sync started for user: {}", currentUsername);

        // Delegate to service
        statsSyncService.fullStatsSync(currentUsername, github, leetcode);

        return ResponseEntity.accepted().body(
                ApiResponse.ok("Sync initiated for " + currentUsername, null, getTraceId())
        );
    }

    /**
     * ME DATA: Returns the stats for the currently authenticated user.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserStats>> getMyStats(Authentication auth) {
        UserStats stats = userStatsRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("Stats record not found for user: " + auth.getName()));

        return ResponseEntity.ok(ApiResponse.ok("My stats retrieved", stats, getTraceId()));
    }

    /**
     * PUBLIC VIEW: Anyone can view a profile by username.
     */
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserStats>> getPublicStats(@PathVariable String username) {
        UserStats stats = userStatsRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Profile stats not found for: " + username));

        return ResponseEntity.ok(ApiResponse.ok("Profile stats retrieved", stats, getTraceId()));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<UserStats>>> getLeaderboard() {
        List<UserStats> topUsers = userStatsRepository.findTop10ByOrderByNexusScoreDesc();
        return ResponseEntity.ok(ApiResponse.ok("Leaderboard fetched", topUsers, getTraceId()));
    }

    private String getTraceId() {
        return (tracer.currentSpan() != null) ? tracer.currentSpan().context().traceId() : "N/A";
    }
}
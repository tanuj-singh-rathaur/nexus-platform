package com.rathaur.nexus.statsservice.service;

import com.rathaur.nexus.statsservice.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/27/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingEngineService {

    private final UserStatsRepository userStatsRepo;

    /**
     * Recalculates the global rank for all users.
     * Fixed Delay: Runs 30 minutes after the PREVIOUS task finishes.
     */
    @Scheduled(fixedDelayString = "300000")
    public void refreshGlobalRankings() {
        log.info("NEXUS-RANK-ENGINE: Starting platform-wide ranking recalculation...");

        try {
            long startTime = System.currentTimeMillis();
            userStatsRepo.updateGlobalRanks();
            long endTime = System.currentTimeMillis();

            log.info("NEXUS-RANK-ENGINE: Successfully updated all global ranks in {}ms", (endTime - startTime));
        } catch (Exception e) {
            log.error("NEXUS-RANK-ENGINE: Critical failure during ranking update!", e);
        }
    }
}
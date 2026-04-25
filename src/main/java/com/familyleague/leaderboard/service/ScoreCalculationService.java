package com.familyleague.leaderboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Async entry-point for score calculations.
 *
 * Each public method is @Async — it runs in a thread-pool thread and immediately
 * delegates to a dedicated @Transactional service bean so the transaction proxy
 * is properly applied (avoids the Spring self-invocation pitfall).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreCalculationService {

    private final MatchScoreTransactionService matchScoreTransactionService;
    private final SeasonScoreTransactionService seasonScoreTransactionService;

    /** Triggered after a match result is published. */
    @Async
    public void calculateScoresAsync(UUID matchId) {
        log.info("Starting match score calculation for matchId={}", matchId);
        try {
            matchScoreTransactionService.execute(matchId);
        } catch (Exception ex) {
            log.error("Match score calculation failed for matchId={}", matchId, ex);
        }
    }

    /** Triggered when an admin closes a season. */
    @Async
    public void calculateSeasonScoresAsync(UUID seasonId) {
        log.info("Starting season score calculation for seasonId={}", seasonId);
        try {
            seasonScoreTransactionService.execute(seasonId);
        } catch (Exception ex) {
            log.error("Season score calculation failed for seasonId={}", seasonId, ex);
        }
    }
}

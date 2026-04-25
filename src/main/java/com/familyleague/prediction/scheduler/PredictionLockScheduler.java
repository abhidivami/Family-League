package com.familyleague.prediction.scheduler;

import com.familyleague.prediction.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that locks match predictions once their predictionLockAt time passes.
 *
 * This ensures is_locked is consistent in the DB independently of result publication.
 * The transactional work is delegated to {@link PredictionService#lockExpiredPredictions()}
 * so it runs inside a proper Spring-managed transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PredictionLockScheduler {

    private final PredictionService predictionService;

    /**
     * Runs every minute by default. Interval is configurable via
     * {@code app.scheduler.lock-check-interval-ms} (default: 60000).
     */
    @Scheduled(fixedDelayString = "${app.scheduler.lock-check-interval-ms:60000}")
    public void lockExpiredMatchPredictions() {
        log.debug("Running prediction lock check");
        predictionService.lockExpiredPredictions();
    }
}

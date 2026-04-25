package com.familyleague.prediction.service;

import com.familyleague.auth.entity.User;
import com.familyleague.auth.repository.UserRepository;
import com.familyleague.common.exception.AppException;
import com.familyleague.common.exception.PredictionLockedException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.match.entity.Match;
import com.familyleague.match.service.MatchService;
import com.familyleague.prediction.dto.PredictionRequest;
import com.familyleague.prediction.dto.PredictionResponse;
import com.familyleague.prediction.entity.Prediction;
import com.familyleague.prediction.repository.PredictionRepository;
import com.familyleague.season.entity.SeasonTeam;
import com.familyleague.season.entity.SeasonTeamPlayer;
import com.familyleague.season.repository.SeasonTeamPlayerRepository;
import com.familyleague.season.repository.SeasonTeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Handles user match prediction submission and retrieval.
 *
 * Rules enforced:
 * - Cannot submit/edit after predictionLockAt
 * - Only own prediction visible before lock; all predictions visible after lock
 * - One prediction row per (user, match); update allowed before lock
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final MatchService matchService;
    private final UserRepository userRepository;
    private final SeasonTeamRepository seasonTeamRepository;
    private final SeasonTeamPlayerRepository seasonTeamPlayerRepository;

    @Transactional
    public PredictionResponse submitOrUpdate(PredictionRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Match match = matchService.findOrThrow(req.matchId());

        // Enforce lock
        if (!match.isPredictionOpen()) {
            throw new PredictionLockedException("Prediction window is closed for match #" + match.getMatchNumber());
        }

        // Resolve picks
        SeasonTeam tossPick = resolveSeasonTeam(req.tossWinnerPickId());
        SeasonTeam matchPick = resolveSeasonTeam(req.matchWinnerPickId());
        SeasonTeamPlayer potmPick = resolveSeasonTeamPlayer(req.potmPickId());

        // Upsert
        Prediction prediction = predictionRepository
                .findByUser_IdAndMatch_IdAndDeletedAtIsNull(user.getId(), match.getId())
                .orElseGet(() -> Prediction.builder().user(user).match(match).build());

        if (prediction.isLocked()) {
            throw new PredictionLockedException("Your prediction is already locked for match #" + match.getMatchNumber());
        }

        prediction.setTossWinnerPick(tossPick);
        prediction.setMatchWinnerPick(matchPick);
        prediction.setPotmPick(potmPick);

        log.info("Prediction saved for user={} match={}", username, match.getMatchNumber());
        return PredictionResponse.from(predictionRepository.save(prediction));
    }

    /** Get own prediction for a match. */
    public PredictionResponse getMyPrediction(UUID matchId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return predictionRepository
                .findByUser_IdAndMatch_IdAndDeletedAtIsNull(user.getId(), matchId)
                .map(PredictionResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No prediction found for this match"));
    }

    /**
     * Get all predictions for a match — only available after prediction window is locked.
     * Returns all non-deleted predictions (not just is_locked=true) because the lock flag
     * is only set async after result publication; time-gating is done above via isPredictionOpen().
     */
    public List<PredictionResponse> getAllPredictionsForMatch(UUID matchId) {
        Match match = matchService.findOrThrow(matchId);
        if (match.isPredictionOpen()) {
            throw new AppException("Predictions are visible only after the prediction window closes",
                    HttpStatus.FORBIDDEN);
        }
        return predictionRepository.findByMatch_IdAndDeletedAtIsNull(matchId)
                .stream().map(PredictionResponse::from).toList();
    }

    /**
     * Called by {@link com.familyleague.prediction.scheduler.PredictionLockScheduler} every minute.
     * Locks any prediction whose match predictionLockAt has passed.
     */
    @Transactional
    public void lockExpiredPredictions() {
        List<Prediction> toLock = predictionRepository.findPredictionsToLock(Instant.now());
        if (!toLock.isEmpty()) {
            toLock.forEach(p -> p.setLocked(true));
            predictionRepository.saveAll(toLock);
            log.info("Locked {} expired match predictions", toLock.size());
        }
    }

    private SeasonTeam resolveSeasonTeam(UUID id) {
        if (id == null) return null;
        return seasonTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SeasonTeam", id));
    }

    private SeasonTeamPlayer resolveSeasonTeamPlayer(UUID id) {
        if (id == null) return null;
        return seasonTeamPlayerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SeasonTeamPlayer", id));
    }
}

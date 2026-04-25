package com.familyleague.prediction.service;

import com.familyleague.auth.entity.User;
import com.familyleague.auth.repository.UserRepository;
import com.familyleague.common.exception.AppException;
import com.familyleague.common.exception.PredictionLockedException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.prediction.dto.RankEntry;
import com.familyleague.prediction.dto.SeasonPredictionRequest;
import com.familyleague.prediction.dto.SeasonPredictionResponse;
import com.familyleague.prediction.entity.SeasonPrediction;
import com.familyleague.prediction.repository.SeasonPredictionRepository;
import com.familyleague.season.entity.Season;
import com.familyleague.season.entity.SeasonTeam;
import com.familyleague.season.repository.SeasonRepository;
import com.familyleague.season.repository.SeasonTeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Season-level prediction: user picks the final standings order for all teams.
 *
 * Rules:
 * - One prediction per (user, season); can be updated until the lock time.
 * - Lock time = first scheduled match in the season − season.leagueLockHours.
 * - predictedRanks must cover every team in the season exactly once,
 *   with positions 1..N (no gaps, no duplicates).
 * - All predictions visible only after the lock time passes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonPredictionService {

    private final SeasonPredictionRepository seasonPredictionRepository;
    private final SeasonRepository seasonRepository;
    private final SeasonTeamRepository seasonTeamRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    // ─── Public API ──────────────────────────────────────────────────────────

    @Transactional
    public SeasonPredictionResponse submitOrUpdate(SeasonPredictionRequest req) {
        String username = currentUsername();
        User user = loadUser(username);
        Season season = loadSeason(req.seasonId());

        if (!isPredictionOpen(season)) {
            throw new PredictionLockedException(
                "Season prediction window is closed for season: " + season.getName());
        }

        List<SeasonTeam> seasonTeams = seasonTeamRepository.findBySeason_Id(season.getId());
        validateRanks(req.predictedRanks(), seasonTeams);

        SeasonPrediction sp = seasonPredictionRepository
            .findByUser_IdAndSeason_IdAndDeletedAtIsNull(user.getId(), season.getId())
            .orElseGet(() -> SeasonPrediction.builder().user(user).season(season).build());

        if (sp.isLocked()) {
            throw new PredictionLockedException(
                "Your season prediction is already locked for season: " + season.getName());
        }

        sp.setPredictedRanks(req.predictedRanks());
        log.info("Season prediction saved for user={} season={}", username, season.getName());
        return SeasonPredictionResponse.from(seasonPredictionRepository.save(sp));
    }

    /** Own prediction — visible at any time. */
    @Transactional(readOnly = true)
    public SeasonPredictionResponse getMyPrediction(UUID seasonId) {
        String username = currentUsername();
        User user = loadUser(username);
        return seasonPredictionRepository
            .findByUser_IdAndSeason_IdAndDeletedAtIsNull(user.getId(), seasonId)
            .map(SeasonPredictionResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No season prediction found for this season"));
    }

    /**
     * All season predictions — visible only after prediction window is locked.
     * (Admin can always view.)
     */
    @Transactional(readOnly = true)
    public List<SeasonPredictionResponse> getAllPredictions(UUID seasonId, boolean isAdmin) {
        Season season = loadSeason(seasonId);
        if (!isAdmin && isPredictionOpen(season)) {
            throw new AppException(
                "Season predictions are visible only after the prediction window closes",
                HttpStatus.FORBIDDEN);
        }
        return seasonPredictionRepository.findBySeason_IdAndDeletedAtIsNull(seasonId)
            .stream()
            .map(SeasonPredictionResponse::from)
            .toList();
    }

    // ─── Lock helper ─────────────────────────────────────────────────────────

    /**
     * A season prediction is open if now is before (first match scheduledAt − leagueLockHours).
     * If no match is scheduled yet, the window is open.
     */
    public boolean isPredictionOpen(Season season) {
        return matchRepository
            .findFirstBySeason_IdAndDeletedAtIsNullOrderByScheduledAtAsc(season.getId())
            .map(firstMatch -> {
                Instant lockAt = firstMatch.getScheduledAt()
                    .minus(season.getLeagueLockHours(), ChronoUnit.HOURS);
                return Instant.now().isBefore(lockAt);
            })
            .orElse(true); // no match scheduled yet → window is open
    }

    // ─── Validation ──────────────────────────────────────────────────────────

    /**
     * Validates that:
     * - ranks cover all teams in the season (no extras, no missing)
     * - positions are exactly 1..N with no gaps or duplicates
     */
    private void validateRanks(List<RankEntry> ranks, List<SeasonTeam> seasonTeams) {
        int teamCount = seasonTeams.size();
        if (ranks.size() != teamCount) {
            throw new AppException(
                "predictedRanks must contain exactly " + teamCount + " entries (one per team in this season)",
                HttpStatus.BAD_REQUEST);
        }

        Set<UUID> teamIds = seasonTeams.stream()
            .map(SeasonTeam::getId)
            .collect(Collectors.toSet());

        Set<UUID> providedTeamIds = ranks.stream()
            .map(RankEntry::seasonTeamId)
            .collect(Collectors.toSet());

        if (!teamIds.equals(providedTeamIds)) {
            throw new AppException(
                "predictedRanks must include all teams in the season — no extras or missing teams allowed",
                HttpStatus.BAD_REQUEST);
        }

        Set<Integer> positions = ranks.stream()
            .map(RankEntry::position)
            .collect(Collectors.toSet());

        Set<Integer> expectedPositions = IntStream.rangeClosed(1, teamCount)
            .boxed()
            .collect(Collectors.toSet());

        if (!positions.equals(expectedPositions)) {
            throw new AppException(
                "Positions must be unique integers from 1 to " + teamCount + " with no gaps",
                HttpStatus.BAD_REQUEST);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Season loadSeason(UUID seasonId) {
        return seasonRepository.findById(seasonId)
            .filter(s -> s.getDeletedAt() == null)
            .orElseThrow(() -> new ResourceNotFoundException("Season not found: " + seasonId));
    }
}

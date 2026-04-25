package com.familyleague.leaderboard.service;

import com.familyleague.leaderboard.entity.SeasonTeamStanding;
import com.familyleague.leaderboard.entity.UserMatchScore;
import com.familyleague.leaderboard.repository.SeasonTeamStandingRepository;
import com.familyleague.leaderboard.repository.UserMatchScoreRepository;
import com.familyleague.match.entity.Match;
import com.familyleague.match.entity.MatchResult;
import com.familyleague.match.repository.MatchResultRepository;
import com.familyleague.prediction.entity.Prediction;
import com.familyleague.prediction.repository.PredictionRepository;
import com.familyleague.season.entity.SeasonTeam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Executes match score and standings updates inside a single transaction.
 * Called by {@link ScoreCalculationService} from an async thread — must be a
 * separate Spring bean so the proxy intercepts the @Transactional boundary.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchScoreTransactionService {

    private final MatchResultRepository matchResultRepository;
    private final PredictionRepository predictionRepository;
    private final UserMatchScoreRepository userMatchScoreRepository;
    private final SeasonTeamStandingRepository standingRepository;

    @Transactional
    public void execute(UUID matchId) {
        MatchResult result = matchResultRepository.findByMatch_Id(matchId)
                .orElseThrow(() -> new IllegalStateException("No result for match " + matchId));

        Match match = result.getMatch();
        UUID seasonId = match.getSeason().getId();
        short matchNumber = match.getMatchNumber();

        // Lock all still-unlocked predictions for this match
        List<Prediction> unlocked = predictionRepository.findUnlockedPredictionsByMatch(matchId);
        unlocked.forEach(p -> p.setLocked(true));
        predictionRepository.saveAll(unlocked);

        // Score every locked prediction (including ones just locked above)
        List<Prediction> allPredictions = predictionRepository.findLockedPredictionsByMatch(matchId);

        for (Prediction pred : allPredictions) {
            short tossPoints = 0;
            short matchPoints = 0;
            short potmPoints = 0;

            if (result.getTossWinnerTeam() != null
                    && pred.getTossWinnerPick() != null
                    && pred.getTossWinnerPick().getId().equals(result.getTossWinnerTeam().getId())) {
                tossPoints = 1;
            }

            if (result.isTie()) {
                if (pred.getMatchWinnerPick() != null) {
                    UUID pick = pred.getMatchWinnerPick().getId();
                    if (pick.equals(match.getHomeTeam().getId()) || pick.equals(match.getAwayTeam().getId())) {
                        matchPoints = 1;
                    }
                }
            } else if (result.getMatchWinnerTeam() != null && pred.getMatchWinnerPick() != null
                    && pred.getMatchWinnerPick().getId().equals(result.getMatchWinnerTeam().getId())) {
                matchPoints = 1;
            }

            if (result.getPotmPlayer() != null && pred.getPotmPick() != null
                    && pred.getPotmPick().getId().equals(result.getPotmPlayer().getId())) {
                potmPoints = 1;
            }

            short total = (short) (tossPoints + matchPoints + potmPoints);

            Optional<UserMatchScore> existing =
                    userMatchScoreRepository.findByUser_IdAndMatch_Id(pred.getUser().getId(), matchId);

            UserMatchScore score = existing.orElseGet(() -> UserMatchScore.builder()
                    .user(pred.getUser())
                    .match(match)
                    .season(match.getSeason())
                    .matchNumber(matchNumber)
                    .build());

            score.setTossPoints(tossPoints);
            score.setMatchWinnerPoints(matchPoints);
            score.setPotmPoints(potmPoints);
            score.setTotalPoints(total);
            score.setUpdatedAt(Instant.now());

            userMatchScoreRepository.save(score);
        }

        updateStandings(result, seasonId);

        log.info("Score calculation complete for matchId={}: {} predictions scored",
                matchId, allPredictions.size());
    }

    private void updateStandings(MatchResult result, UUID seasonId) {
        UUID homeId = result.getMatch().getHomeTeam().getId();
        UUID awayId = result.getMatch().getAwayTeam().getId();

        SeasonTeamStanding homeStanding =
                getOrCreateStanding(seasonId, homeId, result.getMatch().getHomeTeam());
        SeasonTeamStanding awayStanding =
                getOrCreateStanding(seasonId, awayId, result.getMatch().getAwayTeam());

        homeStanding.setMatchesPlayed((short) (homeStanding.getMatchesPlayed() + 1));
        awayStanding.setMatchesPlayed((short) (awayStanding.getMatchesPlayed() + 1));

        if (result.isTie()) {
            homeStanding.setTies((short) (homeStanding.getTies() + 1));
            homeStanding.setPoints((short) (homeStanding.getPoints() + 1));
            awayStanding.setTies((short) (awayStanding.getTies() + 1));
            awayStanding.setPoints((short) (awayStanding.getPoints() + 1));
        } else if (result.getMatchWinnerTeam() != null) {
            UUID winnerId = result.getMatchWinnerTeam().getId();
            if (winnerId.equals(homeId)) {
                homeStanding.setWins((short) (homeStanding.getWins() + 1));
                homeStanding.setPoints((short) (homeStanding.getPoints() + 2));
                awayStanding.setLosses((short) (awayStanding.getLosses() + 1));
            } else {
                awayStanding.setWins((short) (awayStanding.getWins() + 1));
                awayStanding.setPoints((short) (awayStanding.getPoints() + 2));
                homeStanding.setLosses((short) (homeStanding.getLosses() + 1));
            }
        } else {
            // Abandoned / no result
            homeStanding.setNoResults((short) (homeStanding.getNoResults() + 1));
            homeStanding.setPoints((short) (homeStanding.getPoints() + 1));
            awayStanding.setNoResults((short) (awayStanding.getNoResults() + 1));
            awayStanding.setPoints((short) (awayStanding.getPoints() + 1));
        }

        homeStanding.setUpdatedAt(Instant.now());
        awayStanding.setUpdatedAt(Instant.now());

        standingRepository.save(homeStanding);
        standingRepository.save(awayStanding);
    }

    private SeasonTeamStanding getOrCreateStanding(UUID seasonId, UUID seasonTeamId,
                                                    SeasonTeam seasonTeam) {
        return standingRepository.findBySeason_IdAndSeasonTeam_Id(seasonId, seasonTeamId)
                .orElseGet(() -> SeasonTeamStanding.builder()
                        .season(seasonTeam.getSeason())
                        .seasonTeam(seasonTeam)
                        .build());
    }
}

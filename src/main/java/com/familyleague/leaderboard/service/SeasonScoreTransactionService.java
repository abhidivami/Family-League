package com.familyleague.leaderboard.service;

import com.familyleague.leaderboard.entity.UserSeasonScore;
import com.familyleague.leaderboard.repository.SeasonTeamStandingRepository;
import com.familyleague.leaderboard.repository.UserSeasonScoreRepository;
import com.familyleague.prediction.dto.RankEntry;
import com.familyleague.prediction.entity.SeasonPrediction;
import com.familyleague.prediction.repository.SeasonPredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Scores season predictions against the final standings when a season is closed.
 * Runs inside a single transaction — called from {@link ScoreCalculationService}
 * via an async thread, so must be a separate Spring bean for proxy interception.
 *
 * Scoring: 1 point per team whose final standing position exactly matches
 * the user's predicted position.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonScoreTransactionService {

    private final SeasonTeamStandingRepository standingRepository;
    private final SeasonPredictionRepository seasonPredictionRepository;
    private final UserSeasonScoreRepository userSeasonScoreRepository;

    @Transactional
    public void execute(UUID seasonId) {
        // Build final position map: seasonTeamId -> rank (1-based, by points DESC)
        var standings = standingRepository.findBySeason_IdOrderByPointsDescWinsDesc(seasonId);
        Map<UUID, Integer> finalPositions = new HashMap<>();
        for (int i = 0; i < standings.size(); i++) {
            finalPositions.put(standings.get(i).getSeasonTeam().getId(), i + 1);
        }

        List<SeasonPrediction> predictions =
                seasonPredictionRepository.findBySeason_IdAndDeletedAtIsNull(seasonId);

        for (SeasonPrediction sp : predictions) {
            sp.setLocked(true);

            short correct = 0;
            for (RankEntry entry : sp.getPredictedRanks()) {
                Integer actual = finalPositions.get(entry.seasonTeamId());
                if (actual != null && actual == entry.position()) {
                    correct++;
                }
            }

            UserSeasonScore score = userSeasonScoreRepository
                    .findByUser_IdAndSeason_Id(sp.getUser().getId(), seasonId)
                    .orElseGet(() -> UserSeasonScore.builder()
                            .user(sp.getUser())
                            .season(sp.getSeason())
                            .build());

            score.setCorrectPositions(correct);
            score.setTotalPoints(correct);
            userSeasonScoreRepository.save(score);
        }

        seasonPredictionRepository.saveAll(predictions);

        log.info("Season score calculation complete for seasonId={}: {} predictions scored",
                seasonId, predictions.size());
    }
}

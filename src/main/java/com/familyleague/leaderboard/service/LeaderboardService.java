package com.familyleague.leaderboard.service;

import com.familyleague.leaderboard.dto.TeamStandingEntry;
import com.familyleague.leaderboard.dto.UserLeaderboardEntry;
import com.familyleague.leaderboard.entity.UserSeasonScore;
import com.familyleague.leaderboard.repository.SeasonTeamStandingRepository;
import com.familyleague.leaderboard.repository.UserMatchScoreRepository;
import com.familyleague.leaderboard.repository.UserSeasonScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Leaderboard service — two leaderboards:
 *
 * 1. User Prediction Leaderboard (after N matches):
 *    Sum of total_points from user_match_scores where match_number <= N.
 *    For the full season view, season prediction scores are included as well.
 *
 * 2. IPL Points Table (team standings):
 *    Directly from season_team_standings (updated after each result).
 */
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserMatchScoreRepository userMatchScoreRepository;
    private final UserSeasonScoreRepository userSeasonScoreRepository;
    private final SeasonTeamStandingRepository standingRepository;

    /**
     * User leaderboard for a season.
     * Pass afterMatchNumber to get standings after N matches (match scores only).
     * Omit afterMatchNumber (null) for the full season view, which also adds
     * season-prediction scores scored at season close.
     */
    @Transactional(readOnly = true)
    public List<UserLeaderboardEntry> getUserLeaderboard(UUID seasonId, Short afterMatchNumber) {
        if (afterMatchNumber != null) {
            // Historical snapshot: match scores only up to match N
            List<Object[]> rows = userMatchScoreRepository.getUserLeaderboard(seasonId, afterMatchNumber);
            AtomicInteger rank = new AtomicInteger(1);
            return rows.stream()
                    .map(r -> new UserLeaderboardEntry(
                            rank.getAndIncrement(),
                            (UUID) r[0],
                            (String) r[2],
                            (String) r[1],
                            ((Number) r[3]).longValue()))
                    .toList();
        }

        // Full season: match scores + season prediction scores
        // Step 1: collect match scores
        List<Object[]> matchRows = userMatchScoreRepository.getFullSeasonLeaderboard(seasonId);
        Map<UUID, Long> totals = new LinkedHashMap<>();
        Map<UUID, String[]> userInfo = new HashMap<>(); // userId -> [username, displayName]

        for (Object[] r : matchRows) {
            UUID uid = (UUID) r[0];
            totals.put(uid, ((Number) r[3]).longValue());
            userInfo.put(uid, new String[]{(String) r[2], (String) r[1]});
        }

        // Step 2: add season prediction scores
        List<UserSeasonScore> seasonScores = userSeasonScoreRepository.findBySeason_Id(seasonId);
        for (UserSeasonScore ss : seasonScores) {
            UUID uid = ss.getUser().getId();
            totals.merge(uid, (long) ss.getTotalPoints(), Long::sum);
            userInfo.computeIfAbsent(uid, k ->
                    new String[]{ss.getUser().getUsername(), ss.getUser().getDisplayName()});
        }

        // Step 3: sort by total DESC and assign ranks
        AtomicInteger rank = new AtomicInteger(1);
        return totals.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .map(e -> {
                    String[] info = userInfo.getOrDefault(e.getKey(), new String[]{"", ""});
                    return new UserLeaderboardEntry(
                            rank.getAndIncrement(), e.getKey(), info[0], info[1], e.getValue());
                })
                .toList();
    }

    /**
     * IPL-style team points table for a season, ordered by points DESC.
     */
    @Transactional(readOnly = true)
    public List<TeamStandingEntry> getPointsTable(UUID seasonId) {
        var standings = standingRepository.findBySeason_IdOrderByPointsDescWinsDesc(seasonId);
        List<TeamStandingEntry> result = new ArrayList<>();
        int rank = 1;
        for (var s : standings) {
            result.add(new TeamStandingEntry(
                    rank++,
                    s.getSeasonTeam().getId(),
                    s.getSeasonTeam().getTeam().getId(),
                    s.getSeasonTeam().getTeam().getName(),
                    s.getSeasonTeam().getTeam().getShortName(),
                    s.getMatchesPlayed(),
                    s.getWins(),
                    s.getLosses(),
                    s.getTies(),
                    s.getNoResults(),
                    s.getPoints()
            ));
        }
        return result;
    }
}

package com.familyleague.match.dto;

import com.familyleague.match.entity.Match;

import java.time.Instant;
import java.util.UUID;

public record MatchResponse(
    UUID id,
    UUID seasonId,
    UUID homeTeamId,
    String homeTeamName,
    UUID awayTeamId,
    String awayTeamName,
    short matchNumber,
    String venue,
    Instant scheduledAt,
    Instant predictionLockAt,
    String status,
    boolean predictionOpen
) {
    public static MatchResponse from(Match m) {
        return new MatchResponse(
                m.getId(),
                m.getSeason().getId(),
                m.getHomeTeam().getId(),
                m.getHomeTeam().getTeam().getName(),
                m.getAwayTeam().getId(),
                m.getAwayTeam().getTeam().getName(),
                m.getMatchNumber(),
                m.getVenue(),
                m.getScheduledAt(),
                m.getPredictionLockAt(),
                m.getStatus().name(),
                m.isPredictionOpen()
        );
    }
}

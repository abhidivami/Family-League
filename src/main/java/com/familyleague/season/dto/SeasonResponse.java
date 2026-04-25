package com.familyleague.season.dto;

import com.familyleague.season.entity.Season;

import java.time.Instant;
import java.util.UUID;

public record SeasonResponse(
    UUID id,
    UUID leagueId,
    String leagueName,
    String name,
    int year,
    String status,
    short matchLockHours,
    short leagueLockHours,
    Instant createdAt
) {
    public static SeasonResponse from(Season s) {
        return new SeasonResponse(
                s.getId(), s.getLeague().getId(), s.getLeague().getName(),
                s.getName(), s.getYear(), s.getStatus().name(),
                s.getMatchLockHours(), s.getLeagueLockHours(), s.getCreatedAt());
    }
}

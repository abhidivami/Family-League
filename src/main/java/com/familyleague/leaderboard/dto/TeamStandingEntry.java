package com.familyleague.leaderboard.dto;

import java.util.UUID;

/** One row in the IPL-style team points table. */
public record TeamStandingEntry(
    int rank,
    UUID seasonTeamId,
    UUID teamId,
    String teamName,
    String teamShortName,
    short matchesPlayed,
    short wins,
    short losses,
    short ties,
    short noResults,
    short points
) {}

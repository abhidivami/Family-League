package com.familyleague.leaderboard.dto;

import java.util.UUID;

/** One row in the user leaderboard. */
public record UserLeaderboardEntry(
    int rank,
    UUID userId,
    String username,
    String displayName,
    long totalPoints
) {}

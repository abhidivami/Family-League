package com.familyleague.match.dto;

import java.util.UUID;

/**
 * Admin submits actual match result.
 * tossWinnerTeamId may be null if the match was abandoned before the toss.
 */
public record MatchResultRequest(
    UUID tossWinnerTeamId,    // null if abandoned before toss
    UUID matchWinnerTeamId,   // null if tie or abandoned
    UUID potmPlayerId,        // null if tie or abandoned
    boolean tie,
    String resultNotes
) {}

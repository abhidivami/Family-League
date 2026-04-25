package com.familyleague.match.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Admin submits actual match result. */
public record MatchResultRequest(
    @NotNull UUID tossWinnerTeamId,
    UUID matchWinnerTeamId,   // null if tie
    UUID potmPlayerId,
    boolean tie,
    String resultNotes
) {}

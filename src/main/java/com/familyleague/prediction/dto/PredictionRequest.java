package com.familyleague.prediction.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** User submits or updates match predictions. */
public record PredictionRequest(
    @NotNull UUID matchId,
    UUID tossWinnerPickId,      // season_team_id
    UUID matchWinnerPickId,     // season_team_id
    UUID potmPickId             // season_team_player_id
) {}

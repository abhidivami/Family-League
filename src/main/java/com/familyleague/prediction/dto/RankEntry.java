package com.familyleague.prediction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * A single entry in a season prediction: which position a team is predicted to finish.
 */
public record RankEntry(
    @NotNull @Min(1) int position,
    @NotNull UUID seasonTeamId
) {}

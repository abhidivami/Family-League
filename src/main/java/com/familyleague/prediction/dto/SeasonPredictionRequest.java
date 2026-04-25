package com.familyleague.prediction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request body to submit or update a season-level prediction.
 *
 * predictedRanks must contain exactly one entry per team in the season,
 * with contiguous positions starting at 1.
 */
public record SeasonPredictionRequest(
    @NotNull UUID seasonId,
    @NotEmpty @Valid List<RankEntry> predictedRanks
) {}

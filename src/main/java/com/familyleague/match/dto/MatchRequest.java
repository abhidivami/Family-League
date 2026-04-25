package com.familyleague.match.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record MatchRequest(
    @NotNull UUID homeTeamId,
    @NotNull UUID awayTeamId,
    @Min(1) short matchNumber,
    @NotBlank String venue,
    @NotNull Instant scheduledAt
) {}

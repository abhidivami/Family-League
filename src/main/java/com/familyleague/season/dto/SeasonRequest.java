package com.familyleague.season.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeasonRequest(
    @NotBlank String name,
    @NotNull @Min(2000) @Max(2100) Integer year,
    @Min(1) @Max(24) Short matchLockHours,
    @Min(1) @Max(48) Short leagueLockHours
) {}

package com.familyleague.league.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LeagueRequest(
    @NotBlank(message = "League name is required")
    @Size(max = 100)
    String name,

    String description
) {}

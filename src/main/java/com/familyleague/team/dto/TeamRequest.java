package com.familyleague.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 10) String shortName,
    String homeCity,
    String logoUrl
) {}

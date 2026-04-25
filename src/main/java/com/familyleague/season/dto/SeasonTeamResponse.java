package com.familyleague.season.dto;

import java.util.UUID;

public record SeasonTeamResponse(
    UUID id,
    UUID seasonId,
    UUID teamId,
    String teamName,
    String teamShortName
) {}

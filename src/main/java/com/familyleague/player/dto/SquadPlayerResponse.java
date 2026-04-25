package com.familyleague.player.dto;

import java.util.UUID;

public record SquadPlayerResponse(
    UUID seasonTeamPlayerId,
    UUID playerId,
    String playerName,
    String playerRole,
    Short jerseyNumber,
    boolean active
) {}

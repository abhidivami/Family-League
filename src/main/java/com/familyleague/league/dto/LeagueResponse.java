package com.familyleague.league.dto;

import com.familyleague.league.entity.League;

import java.time.Instant;
import java.util.UUID;

public record LeagueResponse(
    UUID id,
    String name,
    String description,
    boolean active,
    Instant createdAt
) {
    public static LeagueResponse from(League l) {
        return new LeagueResponse(l.getId(), l.getName(), l.getDescription(),
                l.isActive(), l.getCreatedAt());
    }
}

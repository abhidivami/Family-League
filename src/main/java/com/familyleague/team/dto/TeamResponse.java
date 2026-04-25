package com.familyleague.team.dto;

import com.familyleague.team.entity.Team;

import java.util.UUID;

public record TeamResponse(
    UUID id,
    String name,
    String shortName,
    String homeCity,
    String logoUrl,
    boolean active
) {
    public static TeamResponse from(Team t) {
        return new TeamResponse(t.getId(), t.getName(), t.getShortName(),
                t.getHomeCity(), t.getLogoUrl(), t.isActive());
    }
}

package com.familyleague.player.dto;

import com.familyleague.season.entity.SeasonTeamPlayer;

import java.util.UUID;

/** One entry in a player's cross-season team history. */
public record PlayerHistoryEntry(
    UUID seasonTeamPlayerId,
    UUID seasonId,
    String seasonName,
    int year,
    UUID seasonTeamId,
    UUID teamId,
    String teamName,
    Short jerseyNumber,
    boolean active
) {
    public static PlayerHistoryEntry from(SeasonTeamPlayer stp) {
        return new PlayerHistoryEntry(
            stp.getId(),
            stp.getSeasonTeam().getSeason().getId(),
            stp.getSeasonTeam().getSeason().getName(),
            stp.getSeasonTeam().getSeason().getYear(),
            stp.getSeasonTeam().getId(),
            stp.getSeasonTeam().getTeam().getId(),
            stp.getSeasonTeam().getTeam().getName(),
            stp.getJerseyNumber(),
            stp.isActive()
        );
    }
}

package com.familyleague.player.dto;

import com.familyleague.player.entity.Player;

import java.time.LocalDate;
import java.util.UUID;

public record PlayerResponse(
    UUID id,
    String name,
    LocalDate dateOfBirth,
    String nationality,
    String battingStyle,
    String bowlingStyle,
    String playerRole,
    boolean active
) {
    public static PlayerResponse from(Player p) {
        return new PlayerResponse(
                p.getId(), p.getName(), p.getDateOfBirth(), p.getNationality(),
                p.getBattingStyle() != null ? p.getBattingStyle().name() : null,
                p.getBowlingStyle() != null ? p.getBowlingStyle().name() : null,
                p.getPlayerRole().name(), p.isActive());
    }
}

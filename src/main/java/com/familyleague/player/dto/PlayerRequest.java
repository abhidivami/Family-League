package com.familyleague.player.dto;

import com.familyleague.player.entity.Player;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PlayerRequest(
    @NotBlank @org.hibernate.validator.constraints.Length(max = 150) String name,
    LocalDate dateOfBirth,
    String nationality,
    Player.BattingStyle battingStyle,
    Player.BowlingStyle bowlingStyle,
    @NotNull Player.PlayerRole playerRole
) {}

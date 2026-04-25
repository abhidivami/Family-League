package com.familyleague.player.entity;

import com.familyleague.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Player — master record independent of team or season.
 * Tracks role, batting/bowling style for POTM eligibility.
 */
@Entity
@Table(name = "player")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "nationality", length = 80)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "batting_style", length = 30)
    private BattingStyle battingStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "bowling_style", length = 30)
    private BowlingStyle bowlingStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "player_role", nullable = false, length = 20)
    private PlayerRole playerRole;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public enum PlayerRole {
        BATSMAN, BOWLER, ALL_ROUNDER, WICKET_KEEPER
    }

    public enum BattingStyle {
        RIGHT_HAND, LEFT_HAND
    }

    public enum BowlingStyle {
        RIGHT_ARM_FAST, RIGHT_ARM_MEDIUM, RIGHT_ARM_SPIN,
        LEFT_ARM_FAST, LEFT_ARM_MEDIUM, LEFT_ARM_SPIN, NA
    }
}

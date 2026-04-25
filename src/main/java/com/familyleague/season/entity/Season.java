package com.familyleague.season.entity;

import com.familyleague.common.audit.BaseEntity;
import com.familyleague.league.entity.League;
import jakarta.persistence.*;
import lombok.*;

/**
 * Season — one instance of a League (e.g., "IPL 2025").
 */
@Entity
@Table(name = "season")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Season extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "year", nullable = false)
    private short year;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeasonStatus status = SeasonStatus.CREATED;

    /**
     * How many hours before a match's scheduled start time predictions are locked.
     * Stored on the season so admins can configure it per season.
     */
    @Column(name = "match_lock_hours", nullable = false)
    private short matchLockHours = 1;

    /**
     * How many hours before the first match of the season the season-level
     * (standings-order) predictions are locked.
     */
    @Column(name = "league_lock_hours", nullable = false)
    private short leagueLockHours = 4;

    /**
     * Lifecycle states for a season.
     * <ul>
     *   <li>{@code CREATED}  — setup phase; teams/players can be added but no matches yet</li>
     *   <li>{@code ACTIVE}   — in progress; matches can be created and played</li>
     *   <li>{@code CLOSED}   — finished; read-only, season scores calculated</li>
     * </ul>
     */
    public enum SeasonStatus {
        CREATED, ACTIVE, CLOSED
    }
}

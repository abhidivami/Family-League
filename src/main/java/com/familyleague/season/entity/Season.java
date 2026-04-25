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

    @Column(name = "match_lock_hours", nullable = false)
    private short matchLockHours = 1;

    @Column(name = "league_lock_hours", nullable = false)
    private short leagueLockHours = 4;

    public enum SeasonStatus {
        CREATED, ACTIVE, CLOSED
    }
}

package com.familyleague.leaderboard.entity;

import com.familyleague.season.entity.Season;
import com.familyleague.season.entity.SeasonTeam;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * IPL-style points table per team per season.
 * Updated after each match result is published.
 * Win = 2 pts, Tie/NR = 1 pt, Loss = 0 pts.
 */
@Entity
@Table(name = "season_team_standing",
       uniqueConstraints = @UniqueConstraint(columnNames = {"season_id", "season_team_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonTeamStanding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_team_id", nullable = false)
    private SeasonTeam seasonTeam;

    @Builder.Default
    @Column(name = "matches_played", nullable = false)
    private short matchesPlayed = 0;

    @Builder.Default
    @Column(name = "wins", nullable = false)
    private short wins = 0;

    @Builder.Default
    @Column(name = "losses", nullable = false)
    private short losses = 0;

    @Builder.Default
    @Column(name = "ties", nullable = false)
    private short ties = 0;

    @Builder.Default
    @Column(name = "no_results", nullable = false)
    private short noResults = 0;

    @Builder.Default
    @Column(name = "points", nullable = false)
    private short points = 0;

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}

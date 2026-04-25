package com.familyleague.match.entity;

import com.familyleague.common.audit.BaseEntity;
import com.familyleague.season.entity.Season;
import com.familyleague.season.entity.SeasonTeam;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * A match in a season between two season-teams.
 * prediction_lock_at is stored explicitly and enforced at app + DB level.
 */
@Entity
@Table(name = "match")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private SeasonTeam homeTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private SeasonTeam awayTeam;

    @Column(name = "match_number", nullable = false)
    private short matchNumber;

    @Column(name = "venue", length = 200)
    private String venue;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    /** System-computed: scheduledAt minus matchLockHours from season config. */
    @Column(name = "prediction_lock_at", nullable = false)
    private Instant predictionLockAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MatchStatus status = MatchStatus.SCHEDULED;

    public boolean isPredictionOpen() {
        return status == MatchStatus.SCHEDULED && Instant.now().isBefore(predictionLockAt);
    }

    public enum MatchStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}

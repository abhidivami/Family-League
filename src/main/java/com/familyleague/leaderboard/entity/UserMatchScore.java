package com.familyleague.leaderboard.entity;

import com.familyleague.auth.entity.User;
import com.familyleague.match.entity.Match;
import com.familyleague.season.entity.Season;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Computed score for one user for one match.
 * match_number is denormalised here to enable efficient "after N matches" queries
 * without joining back to the matches table.
 *
 * Query pattern:
 *   SELECT user_id, SUM(total_points)
 *   FROM user_match_scores
 *   WHERE season_id = ? AND match_number <= ?
 *   GROUP BY user_id
 *   ORDER BY SUM(total_points) DESC
 */
@Entity
@Table(name = "user_match_score",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "match_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMatchScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Column(name = "match_number", nullable = false)
    private short matchNumber;

    @Builder.Default
    @Column(name = "toss_points", nullable = false)
    private short tossPoints = 0;

    @Builder.Default
    @Column(name = "match_winner_points", nullable = false)
    private short matchWinnerPoints = 0;

    @Builder.Default
    @Column(name = "potm_points", nullable = false)
    private short potmPoints = 0;

    @Builder.Default
    @Column(name = "total_points", nullable = false)
    private short totalPoints = 0;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}

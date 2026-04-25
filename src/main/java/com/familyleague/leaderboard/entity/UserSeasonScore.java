package com.familyleague.leaderboard.entity;

import com.familyleague.auth.entity.User;
import com.familyleague.season.entity.Season;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Season-level prediction score for one user.
 * 1 point per team correctly placed in their final standing position.
 * Written once when the admin closes the season.
 */
@Entity
@Table(name = "user_season_score",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "season_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSeasonScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Builder.Default
    @Column(name = "correct_positions", nullable = false)
    private short correctPositions = 0;

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

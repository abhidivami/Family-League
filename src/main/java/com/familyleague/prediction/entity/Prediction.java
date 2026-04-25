package com.familyleague.prediction.entity;

import com.familyleague.auth.entity.User;
import com.familyleague.common.audit.BaseEntity;
import com.familyleague.match.entity.Match;
import com.familyleague.season.entity.SeasonTeam;
import com.familyleague.season.entity.SeasonTeamPlayer;
import jakarta.persistence.*;
import lombok.*;

/**
 * A user's prediction for a single match.
 * One row per (user, match) — holds all three picks.
 * is_locked is set by the system when predictionLockAt passes.
 */
@Entity
@Table(name = "prediction",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "match_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toss_winner_pick_id")
    private SeasonTeam tossWinnerPick;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_winner_pick_id")
    private SeasonTeam matchWinnerPick;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "potm_pick_id")
    private SeasonTeamPlayer potmPick;

    /** Set true by system once predictionLockAt is reached. */
    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;
}

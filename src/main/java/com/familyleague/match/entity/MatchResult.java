package com.familyleague.match.entity;

import com.familyleague.common.audit.BaseEntity;
import com.familyleague.season.entity.SeasonTeam;
import com.familyleague.season.entity.SeasonTeamPlayer;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Actual result of a match — entered by Admin after the real match completes.
 * Triggers async score calculation upon publishing.
 */
@Entity
@Table(name = "match_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "toss_winner_team_id", nullable = false)
    private SeasonTeam tossWinnerTeam;

    /** Null if the result is a tie (is_tie = true). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_winner_team_id")
    private SeasonTeam matchWinnerTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "potm_player_id")
    private SeasonTeamPlayer potmPlayer;

    @Column(name = "is_tie", nullable = false)
    private boolean tie = false;

    @Column(name = "result_notes", columnDefinition = "TEXT")
    private String resultNotes;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by", length = 100)
    private String publishedBy;
}

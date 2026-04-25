package com.familyleague.match.entity;

import com.familyleague.common.audit.BaseEntity;
import com.familyleague.season.entity.SeasonTeamPlayer;
import jakarta.persistence.*;
import lombok.*;

/**
 * Playing XI selection for a match.
 * Each team submits 11 players from their 16-player squad.
 */
@Entity
@Table(name = "match_squad",
       uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "season_team_player_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSquad extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_team_player_id", nullable = false)
    private SeasonTeamPlayer seasonTeamPlayer;

    /** True = selected in Playing XI; false = bench. */
    @Column(name = "is_playing_xi", nullable = false)
    private boolean playingXi = false;
}

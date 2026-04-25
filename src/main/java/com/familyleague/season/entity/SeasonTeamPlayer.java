package com.familyleague.season.entity;

import com.familyleague.common.audit.BaseEntity;
import com.familyleague.player.entity.Player;
import jakarta.persistence.*;
import lombok.*;

/**
 * A player's squad membership for a specific team in a specific season.
 * Squad size = 16.  This table tracks which team a player belonged to in each season —
 * enabling full historical visibility of player-team moves across seasons.
 */
@Entity
@Table(name = "season_team_player",
       uniqueConstraints = @UniqueConstraint(columnNames = {"season_team_id", "player_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonTeamPlayer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_team_id", nullable = false)
    private SeasonTeam seasonTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "jersey_number")
    private Short jerseyNumber;

    /** Allows deactivating a player within a season (e.g., injury). */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}

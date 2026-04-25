package com.familyleague.season.entity;

import com.familyleague.common.audit.BaseEntity;
import com.familyleague.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

/**
 * Links a Team to a Season — represents a team's participation in one season.
 */
@Entity
@Table(name = "season_team",
       uniqueConstraints = @UniqueConstraint(columnNames = {"season_id", "team_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonTeam extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
}

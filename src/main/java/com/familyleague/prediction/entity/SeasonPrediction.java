package com.familyleague.prediction.entity;

import com.familyleague.auth.entity.User;
import com.familyleague.common.audit.BaseEntity;
import com.familyleague.prediction.dto.RankEntry;
import com.familyleague.season.entity.Season;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

/**
 * A user's prediction for the final standings of all teams in a season.
 * One row per (user, season).
 * Locked 4 hours before the first scheduled match of the season.
 */
@Entity
@Table(name = "season_prediction",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "season_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonPrediction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "predicted_ranks", nullable = false, columnDefinition = "jsonb")
    private List<RankEntry> predictedRanks;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private boolean locked = false;
}

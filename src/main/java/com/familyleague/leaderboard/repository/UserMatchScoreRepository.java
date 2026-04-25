package com.familyleague.leaderboard.repository;

import com.familyleague.leaderboard.entity.UserMatchScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMatchScoreRepository extends JpaRepository<UserMatchScore, UUID> {

    Optional<UserMatchScore> findByUser_IdAndMatch_Id(UUID userId, UUID matchId);

    List<UserMatchScore> findBySeason_IdAndMatch_Id(UUID seasonId, UUID matchId);

    /**
     * User leaderboard for a season after N matches.
     * Returns rows ordered by cumulative points DESC.
     */
    @Query("""
        SELECT ums.user.id   AS userId,
               ums.user.displayName AS displayName,
               ums.user.username AS username,
               SUM(ums.totalPoints) AS totalPoints
        FROM UserMatchScore ums
        WHERE ums.season.id = :seasonId
          AND ums.matchNumber <= :afterMatchNumber
        GROUP BY ums.user.id, ums.user.displayName, ums.user.username
        ORDER BY SUM(ums.totalPoints) DESC
    """)
    List<Object[]> getUserLeaderboard(
            @Param("seasonId") UUID seasonId,
            @Param("afterMatchNumber") short afterMatchNumber);

    /** Full season leaderboard (all matches played so far). */
    @Query("""
        SELECT ums.user.id   AS userId,
               ums.user.displayName AS displayName,
               ums.user.username AS username,
               SUM(ums.totalPoints) AS totalPoints
        FROM UserMatchScore ums
        WHERE ums.season.id = :seasonId
        GROUP BY ums.user.id, ums.user.displayName, ums.user.username
        ORDER BY SUM(ums.totalPoints) DESC
    """)
    List<Object[]> getFullSeasonLeaderboard(@Param("seasonId") UUID seasonId);
}

package com.familyleague.season.repository;

import com.familyleague.season.entity.SeasonTeamPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeasonTeamPlayerRepository extends JpaRepository<SeasonTeamPlayer, UUID> {

    List<SeasonTeamPlayer> findBySeasonTeam_Id(UUID seasonTeamId);

    Optional<SeasonTeamPlayer> findBySeasonTeam_IdAndPlayer_Id(UUID seasonTeamId, UUID playerId);

    /** Count active squad players in a season-team (enforce 16-player squad limit). */
    long countBySeasonTeam_IdAndActiveTrue(UUID seasonTeamId);

    /** All squad memberships for a player across all seasons (full history). */
    @Query("""
        SELECT stp FROM SeasonTeamPlayer stp
        JOIN FETCH stp.seasonTeam st
        JOIN FETCH st.season s
        JOIN FETCH st.team
        WHERE stp.player.id = :playerId
        ORDER BY s.year DESC
    """)
    List<SeasonTeamPlayer> findByPlayer_Id(@Param("playerId") UUID playerId);

    /** All active players for a season (across all teams) — for POTM pick in predictions. */
    @Query("""
        SELECT stp FROM SeasonTeamPlayer stp
        JOIN stp.seasonTeam st
        WHERE st.season.id = :seasonId AND stp.active = true
    """)
    List<SeasonTeamPlayer> findActivePlayersBySeason(@Param("seasonId") UUID seasonId);

    /** All active players for a specific match (those with a match_squad entry as playing XI). */
    @Query("""
        SELECT stp FROM SeasonTeamPlayer stp
        JOIN MatchSquad ms ON ms.seasonTeamPlayer.id = stp.id
        WHERE ms.match.id = :matchId AND ms.playingXi = true
    """)
    List<SeasonTeamPlayer> findPlayingXiByMatch(@Param("matchId") UUID matchId);
}

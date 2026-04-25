package com.familyleague.match.repository;

import com.familyleague.match.entity.MatchSquad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchSquadRepository extends JpaRepository<MatchSquad, UUID> {

    List<MatchSquad> findByMatch_Id(UUID matchId);

    List<MatchSquad> findByMatch_IdAndPlayingXiTrue(UUID matchId);

    /** Count playing XI for a specific team in a match (enforce max 11). */
    long countByMatch_IdAndSeasonTeamPlayer_SeasonTeam_IdAndPlayingXiTrue(UUID matchId, UUID seasonTeamId);
}

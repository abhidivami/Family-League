package com.familyleague.leaderboard.repository;

import com.familyleague.leaderboard.entity.SeasonTeamStanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeasonTeamStandingRepository extends JpaRepository<SeasonTeamStanding, UUID> {

    /** IPL points table — ordered by points DESC. */
    List<SeasonTeamStanding> findBySeason_IdOrderByPointsDescWinsDesc(UUID seasonId);

    Optional<SeasonTeamStanding> findBySeason_IdAndSeasonTeam_Id(UUID seasonId, UUID seasonTeamId);
}

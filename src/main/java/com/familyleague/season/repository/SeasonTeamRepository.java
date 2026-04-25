package com.familyleague.season.repository;

import com.familyleague.season.entity.SeasonTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeasonTeamRepository extends JpaRepository<SeasonTeam, UUID> {

    List<SeasonTeam> findBySeason_Id(UUID seasonId);

    Optional<SeasonTeam> findBySeason_IdAndTeam_Id(UUID seasonId, UUID teamId);

    boolean existsBySeason_IdAndTeam_Id(UUID seasonId, UUID teamId);
}

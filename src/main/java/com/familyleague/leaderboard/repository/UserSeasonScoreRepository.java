package com.familyleague.leaderboard.repository;

import com.familyleague.leaderboard.entity.UserSeasonScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSeasonScoreRepository extends JpaRepository<UserSeasonScore, UUID> {

    Optional<UserSeasonScore> findByUser_IdAndSeason_Id(UUID userId, UUID seasonId);

    List<UserSeasonScore> findBySeason_Id(UUID seasonId);
}

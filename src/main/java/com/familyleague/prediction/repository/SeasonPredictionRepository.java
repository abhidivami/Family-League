package com.familyleague.prediction.repository;

import com.familyleague.prediction.entity.SeasonPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeasonPredictionRepository extends JpaRepository<SeasonPrediction, UUID> {

    Optional<SeasonPrediction> findByUser_IdAndSeason_IdAndDeletedAtIsNull(UUID userId, UUID seasonId);

    List<SeasonPrediction> findBySeason_IdAndDeletedAtIsNull(UUID seasonId);
}

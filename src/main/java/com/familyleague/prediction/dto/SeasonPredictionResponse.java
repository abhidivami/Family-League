package com.familyleague.prediction.dto;

import com.familyleague.prediction.entity.SeasonPrediction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SeasonPredictionResponse(
    UUID id,
    UUID userId,
    String username,
    UUID seasonId,
    List<RankEntry> predictedRanks,
    boolean locked,
    Instant createdAt,
    Instant updatedAt
) {
    public static SeasonPredictionResponse from(SeasonPrediction sp) {
        return new SeasonPredictionResponse(
            sp.getId(),
            sp.getUser().getId(),
            sp.getUser().getUsername(),
            sp.getSeason().getId(),
            sp.getPredictedRanks(),
            sp.isLocked(),
            sp.getCreatedAt(),
            sp.getUpdatedAt()
        );
    }
}

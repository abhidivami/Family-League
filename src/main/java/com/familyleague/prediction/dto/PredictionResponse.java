package com.familyleague.prediction.dto;

import com.familyleague.prediction.entity.Prediction;

import java.time.Instant;
import java.util.UUID;

public record PredictionResponse(
    UUID id,
    UUID userId,
    String username,
    UUID matchId,
    short matchNumber,
    UUID tossWinnerPickId,
    String tossWinnerPickName,
    UUID matchWinnerPickId,
    String matchWinnerPickName,
    UUID potmPickId,
    String potmPickName,
    boolean locked,
    Instant createdAt,
    Instant updatedAt
) {
    public static PredictionResponse from(Prediction p) {
        return new PredictionResponse(
                p.getId(),
                p.getUser().getId(),
                p.getUser().getUsername(),
                p.getMatch().getId(),
                p.getMatch().getMatchNumber(),
                p.getTossWinnerPick() != null ? p.getTossWinnerPick().getId() : null,
                p.getTossWinnerPick() != null ? p.getTossWinnerPick().getTeam().getName() : null,
                p.getMatchWinnerPick() != null ? p.getMatchWinnerPick().getId() : null,
                p.getMatchWinnerPick() != null ? p.getMatchWinnerPick().getTeam().getName() : null,
                p.getPotmPick() != null ? p.getPotmPick().getId() : null,
                p.getPotmPick() != null ? p.getPotmPick().getPlayer().getName() : null,
                p.isLocked(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

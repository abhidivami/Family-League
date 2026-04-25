package com.familyleague.match.dto;

import com.familyleague.match.entity.MatchResult;

import java.time.Instant;
import java.util.UUID;

public record MatchResultResponse(
    UUID id,
    UUID matchId,
    UUID tossWinnerTeamId,
    String tossWinnerTeamName,
    UUID matchWinnerTeamId,
    String matchWinnerTeamName,
    UUID potmPlayerId,
    String potmPlayerName,
    boolean tie,
    String resultNotes,
    Instant publishedAt
) {
    public static MatchResultResponse from(MatchResult r) {
        return new MatchResultResponse(
                r.getId(),
                r.getMatch().getId(),
                r.getTossWinnerTeam().getId(),
                r.getTossWinnerTeam().getTeam().getName(),
                r.getMatchWinnerTeam() != null ? r.getMatchWinnerTeam().getId() : null,
                r.getMatchWinnerTeam() != null ? r.getMatchWinnerTeam().getTeam().getName() : null,
                r.getPotmPlayer() != null ? r.getPotmPlayer().getId() : null,
                r.getPotmPlayer() != null ? r.getPotmPlayer().getPlayer().getName() : null,
                r.isTie(),
                r.getResultNotes(),
                r.getPublishedAt()
        );
    }
}

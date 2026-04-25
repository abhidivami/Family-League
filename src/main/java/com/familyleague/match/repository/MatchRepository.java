package com.familyleague.match.repository;

import com.familyleague.match.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {

    Page<Match> findBySeason_IdAndDeletedAtIsNull(UUID seasonId, Pageable pageable);

    Optional<Match> findBySeason_IdAndMatchNumberAndDeletedAtIsNull(UUID seasonId, short matchNumber);

    /** First scheduled match in a season — used to compute league prediction lock. */
    Optional<Match> findFirstBySeason_IdAndDeletedAtIsNullOrderByScheduledAtAsc(UUID seasonId);

    /** Matches whose prediction window is open and are approaching lock time. */
    @Query("""
        SELECT m FROM Match m
        WHERE m.season.id = :seasonId
          AND m.deletedAt IS NULL
          AND m.status = 'SCHEDULED'
          AND m.predictionLockAt BETWEEN :from AND :to
    """)
    List<Match> findMatchesApproachingLock(
            @Param("seasonId") UUID seasonId,
            @Param("from") Instant from,
            @Param("to") Instant to);

    /** Ordered list of completed matches (for leaderboard snapshots). */
    List<Match> findBySeason_IdAndStatusAndDeletedAtIsNullOrderByMatchNumberAsc(
            UUID seasonId, Match.MatchStatus status);
}

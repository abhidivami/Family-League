package com.familyleague.prediction.repository;

import com.familyleague.prediction.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, UUID> {

    Optional<Prediction> findByUser_IdAndMatch_IdAndDeletedAtIsNull(UUID userId, UUID matchId);

    /** All predictions for a match (visible to all after lock). */
    @Query("""
        SELECT p FROM Prediction p
        WHERE p.match.id = :matchId
          AND p.deletedAt IS NULL
          AND p.locked = true
    """)
    List<Prediction> findLockedPredictionsByMatch(@Param("matchId") UUID matchId);

    /** All unlocked predictions for a match (for scheduled lock job). */
    @Query("""
        SELECT p FROM Prediction p
        WHERE p.match.id = :matchId
          AND p.deletedAt IS NULL
          AND p.locked = false
    """)
    List<Prediction> findUnlockedPredictionsByMatch(@Param("matchId") UUID matchId);

    /** All non-deleted predictions for a match regardless of locked state. */
    List<Prediction> findByMatch_IdAndDeletedAtIsNull(UUID matchId);

    /** Unlocked predictions whose match lock time has already passed — used by the lock scheduler. */
    @Query("""
        SELECT p FROM Prediction p
        WHERE p.locked = false
          AND p.deletedAt IS NULL
          AND p.match.predictionLockAt < :now
          AND p.match.deletedAt IS NULL
    """)
    List<Prediction> findPredictionsToLock(@Param("now") java.time.Instant now);

    /** Users who have NOT submitted a prediction for a match. */
    @Query("""
        SELECT u.id FROM User u
        WHERE u.deletedAt IS NULL AND u.active = true
          AND u.id NOT IN (
              SELECT p.user.id FROM Prediction p
              WHERE p.match.id = :matchId AND p.deletedAt IS NULL
          )
    """)
    List<UUID> findUsersWithoutPredictionForMatch(@Param("matchId") UUID matchId);
}

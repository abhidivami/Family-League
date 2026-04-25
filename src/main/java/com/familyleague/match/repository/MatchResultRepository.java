package com.familyleague.match.repository;

import com.familyleague.match.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    Optional<MatchResult> findByMatch_Id(UUID matchId);

    boolean existsByMatch_Id(UUID matchId);
}

package com.familyleague.season.repository;

import com.familyleague.season.entity.Season;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeasonRepository extends JpaRepository<Season, UUID> {

    Page<Season> findByLeague_IdAndDeletedAtIsNull(UUID leagueId, Pageable pageable);

    List<Season> findByLeague_IdAndDeletedAtIsNull(UUID leagueId);
}

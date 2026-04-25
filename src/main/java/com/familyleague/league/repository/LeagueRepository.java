package com.familyleague.league.repository;

import com.familyleague.league.entity.League;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LeagueRepository extends JpaRepository<League, UUID> {

    Page<League> findByDeletedAtIsNull(Pageable pageable);

    Page<League> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name, Pageable pageable);
}

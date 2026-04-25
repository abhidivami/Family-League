package com.familyleague.team.repository;

import com.familyleague.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    Page<Team> findByDeletedAtIsNull(Pageable pageable);

    Page<Team> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name, Pageable pageable);
}

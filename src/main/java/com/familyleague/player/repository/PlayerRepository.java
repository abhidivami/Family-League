package com.familyleague.player.repository;

import com.familyleague.player.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    Page<Player> findByDeletedAtIsNull(Pageable pageable);

    Page<Player> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name, Pageable pageable);
}

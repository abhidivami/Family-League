package com.familyleague.league.entity;

import com.familyleague.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * League — the umbrella entity (e.g., "IPL").
 * One League has many Seasons.
 */
@Entity
@Table(name = "league")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class League extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}

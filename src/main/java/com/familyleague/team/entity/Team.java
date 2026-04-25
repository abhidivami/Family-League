package com.familyleague.team.entity;

import com.familyleague.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Team — independent of any season.
 * The same team (e.g., Mumbai Indians) participates across many seasons.
 */
@Entity
@Table(name = "team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "short_name", nullable = false, length = 10)
    private String shortName;

    @Column(name = "home_city", length = 100)
    private String homeCity;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}

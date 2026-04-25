-- ============================================================
-- V1 : Family League — Full Schema
-- PostgreSQL
-- Convention: singular table names (exception: users)
-- Enums stored as VARCHAR with CHECK constraints (avoids JDBC casting issues)
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─── USERS ──────────────────────────────────────────────────
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100),
    avatar_url    VARCHAR(500),
    role          VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER'
                      CHECK (role IN ('ROLE_ADMIN', 'ROLE_USER')),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    deleted_at    TIMESTAMPTZ
);

-- ─── LEAGUE ─────────────────────────────────────────────────
CREATE TABLE league (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted_at  TIMESTAMPTZ
);

-- ─── SEASON ─────────────────────────────────────────────────
CREATE TABLE season (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    league_id         UUID         NOT NULL REFERENCES league(id),
    name              VARCHAR(100) NOT NULL,
    year              SMALLINT     NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'CREATED'
                          CHECK (status IN ('CREATED', 'ACTIVE', 'CLOSED')),
    match_lock_hours  SMALLINT     NOT NULL DEFAULT 1,
    league_lock_hours SMALLINT     NOT NULL DEFAULT 4,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100),
    deleted_at        TIMESTAMPTZ
);

-- ─── TEAM ───────────────────────────────────────────────────
CREATE TABLE team (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    short_name VARCHAR(10)  NOT NULL,
    home_city  VARCHAR(100),
    logo_url   VARCHAR(500),
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMPTZ
);

-- ─── SEASON_TEAM ────────────────────────────────────────────
CREATE TABLE season_team (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id  UUID NOT NULL REFERENCES season(id),
    team_id    UUID NOT NULL REFERENCES team(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE (season_id, team_id)
);

-- ─── PLAYER ─────────────────────────────────────────────────
CREATE TABLE player (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(150) NOT NULL,
    date_of_birth DATE,
    nationality   VARCHAR(80),
    batting_style VARCHAR(30)  CHECK (batting_style IN ('RIGHT_HAND', 'LEFT_HAND')),
    bowling_style VARCHAR(30)  CHECK (bowling_style IN ('RIGHT_ARM_FAST', 'RIGHT_ARM_MEDIUM',
                                     'RIGHT_ARM_SPIN', 'LEFT_ARM_FAST', 'LEFT_ARM_MEDIUM',
                                     'LEFT_ARM_SPIN', 'NA')),
    player_role   VARCHAR(20)  NOT NULL
                      CHECK (player_role IN ('BATSMAN', 'BOWLER', 'ALL_ROUNDER', 'WICKET_KEEPER')),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100),
    deleted_at    TIMESTAMPTZ
);

-- ─── SEASON_TEAM_PLAYER ─────────────────────────────────────
CREATE TABLE season_team_player (
    id             UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    season_team_id UUID     NOT NULL REFERENCES season_team(id),
    player_id      UUID     NOT NULL REFERENCES player(id),
    jersey_number  SMALLINT,
    is_active      BOOLEAN  NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    UNIQUE (season_team_id, player_id)
);

-- ─── MATCH ──────────────────────────────────────────────────
CREATE TABLE match (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id          UUID        NOT NULL REFERENCES season(id),
    home_team_id       UUID        NOT NULL REFERENCES season_team(id),
    away_team_id       UUID        NOT NULL REFERENCES season_team(id),
    match_number       SMALLINT    NOT NULL,
    venue              VARCHAR(200),
    scheduled_at       TIMESTAMPTZ NOT NULL,
    prediction_lock_at TIMESTAMPTZ NOT NULL,
    status             VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
                           CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by         VARCHAR(100),
    updated_by         VARCHAR(100),
    deleted_at         TIMESTAMPTZ,
    CONSTRAINT chk_different_teams CHECK (home_team_id <> away_team_id)
);

CREATE INDEX idx_match_season   ON match(season_id);
CREATE INDEX idx_match_schedule ON match(scheduled_at);

-- ─── MATCH_SQUAD ────────────────────────────────────────────
CREATE TABLE match_squad (
    id                    UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id              UUID    NOT NULL REFERENCES match(id),
    season_team_player_id UUID    NOT NULL REFERENCES season_team_player(id),
    is_playing_xi         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    UNIQUE (match_id, season_team_player_id)
);

-- ─── MATCH_RESULT ───────────────────────────────────────────
CREATE TABLE match_result (
    id                   UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id             UUID    UNIQUE NOT NULL REFERENCES match(id),
    toss_winner_team_id  UUID    NOT NULL REFERENCES season_team(id),
    match_winner_team_id UUID             REFERENCES season_team(id),
    potm_player_id       UUID             REFERENCES season_team_player(id),
    is_tie               BOOLEAN NOT NULL DEFAULT FALSE,
    result_notes         TEXT,
    published_at         TIMESTAMPTZ,
    published_by         VARCHAR(100),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100)
);

-- ─── PREDICTION ─────────────────────────────────────────────
CREATE TABLE prediction (
    id                   UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID    NOT NULL REFERENCES users(id),
    match_id             UUID    NOT NULL REFERENCES match(id),
    toss_winner_pick_id  UUID             REFERENCES season_team(id),
    match_winner_pick_id UUID             REFERENCES season_team(id),
    potm_pick_id         UUID             REFERENCES season_team_player(id),
    is_locked            BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by           VARCHAR(100),
    updated_by           VARCHAR(100),
    deleted_at           TIMESTAMPTZ,
    UNIQUE (user_id, match_id)
);

CREATE INDEX idx_prediction_match ON prediction(match_id);
CREATE INDEX idx_prediction_user  ON prediction(user_id);

-- ─── USER_MATCH_SCORE ───────────────────────────────────────
CREATE TABLE user_match_score (
    id                  UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID     NOT NULL REFERENCES users(id),
    match_id            UUID     NOT NULL REFERENCES match(id),
    season_id           UUID     NOT NULL REFERENCES season(id),
    match_number        SMALLINT NOT NULL,
    toss_points         SMALLINT NOT NULL DEFAULT 0,
    match_winner_points SMALLINT NOT NULL DEFAULT 0,
    potm_points         SMALLINT NOT NULL DEFAULT 0,
    total_points        SMALLINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, match_id)
);

CREATE INDEX idx_ums_season_match ON user_match_score(season_id, match_number);
CREATE INDEX idx_ums_user_season  ON user_match_score(user_id, season_id);

-- ─── SEASON_PREDICTION ──────────────────────────────────────
CREATE TABLE season_prediction (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID    NOT NULL REFERENCES users(id),
    season_id       UUID    NOT NULL REFERENCES season(id),
    predicted_ranks JSONB   NOT NULL,
    is_locked       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted_at      TIMESTAMPTZ,
    UNIQUE (user_id, season_id)
);

-- ─── SEASON_TEAM_STANDING ───────────────────────────────────
CREATE TABLE season_team_standing (
    id             UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id      UUID     NOT NULL REFERENCES season(id),
    season_team_id UUID     NOT NULL REFERENCES season_team(id),
    matches_played SMALLINT NOT NULL DEFAULT 0,
    wins           SMALLINT NOT NULL DEFAULT 0,
    losses         SMALLINT NOT NULL DEFAULT 0,
    ties           SMALLINT NOT NULL DEFAULT 0,
    no_results     SMALLINT NOT NULL DEFAULT 0,
    points         SMALLINT NOT NULL DEFAULT 0,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (season_id, season_team_id)
);

CREATE INDEX idx_standing_season ON season_team_standing(season_id, points DESC);

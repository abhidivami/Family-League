-- ============================================================
-- V2 : Add user_season_score table
-- Stores the score a user earns from their season standing prediction.
-- Populated asynchronously when an admin closes a season.
-- ============================================================

CREATE TABLE user_season_score (
    id                UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID     NOT NULL REFERENCES users(id),
    season_id         UUID     NOT NULL REFERENCES season(id),
    correct_positions SMALLINT NOT NULL DEFAULT 0,
    total_points      SMALLINT NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, season_id)
);

CREATE INDEX idx_uss_season ON user_season_score(season_id, total_points DESC);

-- ============================================================
-- V3 : Add missing deleted_at columns
-- season_team, season_team_player, match_squad, and match_result
-- all extend BaseEntity in Java but were missing deleted_at in DDL.
-- ============================================================

ALTER TABLE season_team       ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE season_team_player ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE match_squad        ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE match_result       ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

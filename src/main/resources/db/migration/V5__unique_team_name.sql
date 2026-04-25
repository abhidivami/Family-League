-- Remove duplicate active teams that have no season_team references (created by repeated test runs).
-- For each duplicate group, keep the oldest row and delete the newer orphan rows.
DELETE FROM team t
WHERE deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM season_team WHERE team_id = t.id)
  AND t.id NOT IN (
      SELECT DISTINCT ON (lower(name)) id
      FROM team
      WHERE deleted_at IS NULL
      ORDER BY lower(name), created_at
  );

-- Enforce case-insensitive uniqueness on active (non-deleted) team names at the DB level.
CREATE UNIQUE INDEX uq_team_name_active ON team (lower(name)) WHERE deleted_at IS NULL;

-- Allow toss_winner_team_id to be NULL for matches abandoned before the toss
ALTER TABLE match_result ALTER COLUMN toss_winner_team_id DROP NOT NULL;

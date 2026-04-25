# Family League — Architecture & Feature Overview

## What it is

Family League is a Spring Boot REST API for running an IPL prediction game within a family/friends group. Users predict match outcomes each game, accumulate points, and compete on a leaderboard. An admin manages the IPL structure (leagues, seasons, teams, players, matches) and publishes results.

---

## Tech Stack

| Layer | Choice |
|---|---|
| Runtime | Java 25, Spring Boot 4.0.6 |
| Web | Spring MVC (Tomcat) |
| Persistence | Spring Data JPA + Hibernate 7 / PostgreSQL 18 |
| Migrations | Flyway 11 |
| Security | Spring Security 7 — stateless JWT (HS256, JJWT) |
| Async | Spring `@Async` (SimpleAsyncTaskExecutor) |
| Scheduling | Spring `@Scheduled` |
| Docs | SpringDoc OpenAPI 2 (`/swagger-ui.html`) |
| Monitoring | Spring Boot Actuator (`/actuator/health`, `/actuator/metrics`) |

---

## Domain Model

```
League  1──* Season  1──* SeasonTeam  *──* Player  (via SeasonTeamPlayer)
                         │
                         └──* Match  1──1 MatchResult
                                     │
                                     └──* MatchSquad (playing XI per team)

User  *──* Match     → Prediction
User  *──* Season    → SeasonPrediction
User  *──* Match     → UserMatchScore     (computed after result)
User  *──* Season    → UserSeasonScore    (computed after season close)
SeasonTeam  *──* Season → SeasonTeamStanding (IPL points table)
```

### Entities and their key responsibilities

| Entity | Table | Purpose |
|---|---|---|
| `User` | `users` | Auth identity; role=ROLE_USER or ROLE_ADMIN |
| `League` | `league` | Top-level container (e.g. "IPL") |
| `Season` | `season` | One per year; lifecycle CREATED→ACTIVE→CLOSED |
| `Team` | `team` | Canonical team record (shared across seasons) |
| `SeasonTeam` | `season_team` | Team participating in one specific season |
| `Player` | `player` | Canonical player record |
| `SeasonTeamPlayer` | `season_team_player` | Player in a season's team roster |
| `Match` | `match` | Scheduled fixture; holds `predictionLockAt` |
| `MatchSquad` | `match_squad` | Playing XI per team per match |
| `MatchResult` | `match_result` | Published result (toss, winner, POTM) |
| `Prediction` | `prediction` | User's per-match picks (toss/winner/POTM) |
| `SeasonPrediction` | `season_prediction` | User's full final-standings prediction (JSONB) |
| `UserMatchScore` | `user_match_score` | Scored match prediction (1 pt per correct pick) |
| `UserSeasonScore` | `user_season_score` | Scored season prediction (1 pt per exact position) |
| `SeasonTeamStanding` | `season_team_standing` | IPL-style points table (W=2pts, Tie/NR=1pt) |

All entities except `UserMatchScore`/`UserSeasonScore`/`SeasonTeamStanding` extend `BaseEntity` which adds `id`, `createdAt`, `updatedAt`, `deletedAt` (soft delete).

---

## Prediction Rules

### Match Prediction
- Window: open from match creation until `predictionLockAt` (default: scheduledAt − 1 h)
- One row per (user, match); edits allowed before lock
- Picks: toss winner, match winner, Player of the Match (POTM)
- Scoring: 1 point per correct pick → max 3 pts/match
- Visibility: own prediction always visible; all predictions visible only after lock

### Season Prediction
- Window: open until `firstMatchScheduledAt − leagueLockHours` (default 4 h)
- One row per (user, season); edits allowed before lock
- Pick: full final standings ordered list (position 1–N for all season teams)
- Scoring: 1 point per team at the exact correct position → max N pts/season
- Triggered: async after admin closes the season

---

## API Surface

### Auth
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create user account |
| POST | `/api/auth/login` | Public | Get JWT access token |
| POST | `/api/auth/refresh` | Public | Refresh access token |

### League & Season
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/admin/leagues` | Admin | Create league |
| GET | `/api/leagues` | User | List leagues (paginated) |
| GET | `/api/leagues/{id}` | User | Get league |
| POST | `/api/admin/leagues/{id}/seasons` | Admin | Create season |
| GET | `/api/leagues/{id}/seasons` | User | List seasons |
| GET | `/api/seasons/{id}` | User | Get season |
| PATCH | `/api/admin/seasons/{id}/activate` | Admin | CREATED → ACTIVE |
| PATCH | `/api/admin/seasons/{id}/close` | Admin | ACTIVE → CLOSED (triggers season scoring) |

### Teams & Players
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/admin/teams` | Admin | Create team |
| GET | `/api/teams` | User | List teams |
| POST | `/api/admin/seasons/{id}/teams` | Admin | Add team to season |
| POST | `/api/admin/players` | Admin | Create player |
| GET | `/api/players` | User | List players |
| POST | `/api/admin/season-teams/{id}/players` | Admin | Add player to season team |

### Matches
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/admin/seasons/{id}/matches` | Admin | Schedule match |
| GET | `/api/seasons/{id}/matches` | User | List matches |
| GET | `/api/matches/{id}` | User | Get match |
| PATCH | `/api/admin/matches/{id}/squad/{stpId}` | Admin | Set/unset playing XI |
| POST | `/api/admin/matches/{id}/result` | Admin | Publish result (triggers async scoring) |
| GET | `/api/matches/{id}/result` | User | Get published result |

### Predictions
| Method | Path | Access | Description |
|---|---|---|---|
| POST | `/api/predictions/matches` | User | Submit/update match prediction |
| GET | `/api/predictions/matches/{id}/mine` | User | Get own match prediction |
| GET | `/api/predictions/matches/{id}/all` | User | All predictions (only after lock) |
| POST | `/api/season-predictions` | User | Submit/update season prediction |
| GET | `/api/season-predictions/seasons/{id}/mine` | User | Get own season prediction |
| GET | `/api/season-predictions/seasons/{id}/all` | Admin | All season predictions (after lock) |

### Leaderboard
| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/api/leaderboard/seasons/{id}/users` | User | User leaderboard (add `?afterMatch=N` for snapshot) |
| GET | `/api/leaderboard/seasons/{id}/points-table` | User | IPL-style team points table |

---

## Async Processing & Scheduling

### Match Score Calculation
Triggered: immediately after admin publishes a match result (runs **after** the HTTP transaction commits via `TransactionSynchronization.afterCommit()`).

Flow:
1. `MatchService.publishResult()` saves `MatchResult` + updates match status → commits
2. `ScoreCalculationService.calculateScoresAsync()` runs in async thread
3. `MatchScoreTransactionService.execute()` (own `@Transactional`):
   - Locks all open predictions for the match (`is_locked = true`)
   - Scores each prediction: 1 pt per correct pick
   - Saves `UserMatchScore` rows
   - Upserts `SeasonTeamStanding` (W=2pts, Tie/NR=1pt)

### Season Score Calculation
Triggered: after admin closes a season (same `afterCommit()` pattern).

Flow:
1. Reads final `SeasonTeamStanding` to build position map
2. Compares each `SeasonPrediction.predictedRanks` against actual positions
3. Awards 1 pt per exact position match → saves `UserSeasonScore`

### Prediction Lock Scheduler
`PredictionLockScheduler` runs every 60 seconds and calls `PredictionService.lockExpiredPredictions()` to batch-lock any predictions whose `match.predictionLockAt` has passed.

---

## Security

- All endpoints require a valid JWT `Bearer` token except `/api/auth/**`
- Roles enforced with `@PreAuthorize`: admin-only endpoints require `ROLE_ADMIN`
- Passwords hashed with BCrypt
- JWT secret and DB credentials are externalised via environment variables (`JWT_SECRET`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)

---

## Database Migrations (Flyway)

| Version | Description |
|---|---|
| V1 | Full initial schema (all tables) |
| V2 | Add `user_season_score` table |
| V3 | Add missing `deleted_at` columns to `season_team`, `season_team_player`, `match_squad`, `match_result` |

---

## Known Bugs Fixed vs. Original Copilot Code

| # | Bug | Fix |
|---|---|---|
| 1 | `@Async calculateScoresAsync` called `@Transactional calculateScores` on `this` — proxy bypass | Extracted DB logic into separate bean (`MatchScoreTransactionService`) |
| 2 | `getAllPredictionsForMatch` used `findLockedPredictions` — returned empty between lock time and result | Changed to `findByMatch_IdAndDeletedAtIsNull` |
| 3 | `createMatch` didn't validate both teams belong to the season | Added season membership check |
| 4 | `publishResult` didn't validate POTM player belongs to a playing team | Added team membership check |
| 5 | DB credentials and JWT secret hardcoded in `application.properties` | Externalised with `${ENV_VAR:default}` pattern |
| 6 | Async score calculation fired before transaction commit — race condition | Moved trigger to `TransactionSynchronization.afterCommit()` |
| 7 | `UserMatchScore`, `SeasonTeamStanding`, `UserSeasonScore` Lombok `@Builder` ignored field defaults — null timestamp inserts | Added `@Builder.Default` to all affected fields |
| 8 | `SeasonPrediction.predictedRanks` used `@Convert(String)` against JSONB column — type mismatch | Replaced with `@JdbcTypeCode(SqlTypes.JSON)` (Hibernate 6 native JSON mapping) |
| 9 | `season_team`, `season_team_player`, `match_squad`, `match_result` missing `deleted_at` in DDL despite extending `BaseEntity` | V3 migration adds the columns |

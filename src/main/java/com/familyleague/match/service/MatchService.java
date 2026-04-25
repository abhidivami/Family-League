package com.familyleague.match.service;

import com.familyleague.common.exception.AppException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.leaderboard.service.ScoreCalculationService;
import com.familyleague.match.dto.*;
import com.familyleague.match.entity.Match;
import com.familyleague.match.entity.MatchResult;
import com.familyleague.match.entity.MatchSquad;
import com.familyleague.match.repository.MatchRepository;
import com.familyleague.match.repository.MatchResultRepository;
import com.familyleague.match.repository.MatchSquadRepository;
import com.familyleague.season.entity.Season;
import com.familyleague.season.entity.SeasonTeam;
import com.familyleague.season.entity.SeasonTeamPlayer;
import com.familyleague.season.repository.SeasonTeamPlayerRepository;
import com.familyleague.season.repository.SeasonTeamRepository;
import com.familyleague.season.service.SeasonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Core service for match management.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Creating matches and computing {@code predictionLockAt} from season config</li>
 *   <li>Managing the Playing XI (11 players per team per match)</li>
 *   <li>Publishing match results and triggering async score recalculation</li>
 * </ul>
 *
 * Score calculation is fired inside a {@link TransactionSynchronization#afterCommit()} callback
 * so the result row is committed and visible to the async thread before it reads.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchResultRepository matchResultRepository;
    private final MatchSquadRepository matchSquadRepository;
    private final SeasonTeamRepository seasonTeamRepository;
    private final SeasonTeamPlayerRepository seasonTeamPlayerRepository;
    private final SeasonService seasonService;
    private final ScoreCalculationService scoreCalculationService;

    /**
     * Create a match in an open season.
     * {@code predictionLockAt} is derived as {@code scheduledAt - season.matchLockHours}.
     */
    @Transactional
    public MatchResponse createMatch(UUID seasonId, MatchRequest req) {
        Season season = seasonService.findOrThrow(seasonId);
        requireSeasonOpen(season);

        SeasonTeam home = findSeasonTeamOrThrow(req.homeTeamId());
        SeasonTeam away = findSeasonTeamOrThrow(req.awayTeamId());

        if (home.getId().equals(away.getId())) {
            throw new AppException("Home and away teams must be different", HttpStatus.BAD_REQUEST);
        }

        if (!home.getSeason().getId().equals(seasonId) || !away.getSeason().getId().equals(seasonId)) {
            throw new AppException("Both teams must belong to the specified season", HttpStatus.BAD_REQUEST);
        }

        Instant lockAt = req.scheduledAt()
                .minus(season.getMatchLockHours(), ChronoUnit.HOURS);

        Match match = Match.builder()
                .season(season)
                .homeTeam(home)
                .awayTeam(away)
                .matchNumber(req.matchNumber())
                .venue(req.venue())
                .scheduledAt(req.scheduledAt())
                .predictionLockAt(lockAt)
                .status(Match.MatchStatus.SCHEDULED)
                .build();

        return MatchResponse.from(matchRepository.save(match));
    }

    /** Paginated list of non-deleted matches for a season. */
    public Page<MatchResponse> listBySeason(UUID seasonId, Pageable pageable) {
        return matchRepository.findBySeason_IdAndDeletedAtIsNull(seasonId, pageable)
                .map(MatchResponse::from);
    }

    /** Fetch a single match by ID. */
    public MatchResponse getById(UUID id) {
        return MatchResponse.from(findOrThrow(id));
    }

    /**
     * Admin sets the Playing XI (11 players per team) for a match.
     */
    @Transactional
    public void setPlayingXi(UUID matchId, UUID seasonTeamPlayerId, boolean isPlayingXi) {
        Match match = findOrThrow(matchId);
        requireSeasonOpen(match.getSeason());
        SeasonTeamPlayer stp = seasonTeamPlayerRepository.findById(seasonTeamPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException("SeasonTeamPlayer", seasonTeamPlayerId));

        if (isPlayingXi) {
            UUID stId = stp.getSeasonTeam().getId();
            long currentXi = matchSquadRepository
                    .countByMatch_IdAndSeasonTeamPlayer_SeasonTeam_IdAndPlayingXiTrue(matchId, stId);
            if (currentXi >= 11) {
                throw new AppException("Playing XI is full (max 11 players per team)", HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        // Upsert
        matchSquadRepository.findByMatch_Id(matchId).stream()
                .filter(ms -> ms.getSeasonTeamPlayer().getId().equals(seasonTeamPlayerId))
                .findFirst()
                .ifPresentOrElse(
                        ms -> { ms.setPlayingXi(isPlayingXi); matchSquadRepository.save(ms); },
                        () -> {
                            MatchSquad ms = MatchSquad.builder()
                                    .match(match).seasonTeamPlayer(stp).playingXi(isPlayingXi).build();
                            matchSquadRepository.save(ms);
                        }
                );
    }

    /**
     * Admin publishes the result.
     * Triggers async score recalculation + standings update.
     */
    @Transactional
    public MatchResultResponse publishResult(UUID matchId, MatchResultRequest req) {
        Match match = findOrThrow(matchId);
        requireSeasonOpen(match.getSeason());

        if (matchResultRepository.existsByMatch_Id(matchId)) {
            throw new AppException("Result already published for this match", HttpStatus.CONFLICT);
        }

        // Ensure both teams have exactly 11 players in the Playing XI
        long homeXi = matchSquadRepository.countByMatch_IdAndSeasonTeamPlayer_SeasonTeam_IdAndPlayingXiTrue(
                matchId, match.getHomeTeam().getId());
        long awayXi = matchSquadRepository.countByMatch_IdAndSeasonTeamPlayer_SeasonTeam_IdAndPlayingXiTrue(
                matchId, match.getAwayTeam().getId());

        if (homeXi != 11 || awayXi != 11) {
            throw new AppException(
                    String.format("Both teams must have exactly 11 players in the Playing XI before publishing a result. " +
                            "Home: %d/11, Away: %d/11", homeXi, awayXi),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        SeasonTeam tossWinner = req.tossWinnerTeamId() != null
                ? findSeasonTeamOrThrow(req.tossWinnerTeamId()) : null;
        SeasonTeam matchWinner = req.matchWinnerTeamId() != null
                ? findSeasonTeamOrThrow(req.matchWinnerTeamId()) : null;
        SeasonTeamPlayer potm = null;
        if (req.potmPlayerId() != null) {
            potm = seasonTeamPlayerRepository.findById(req.potmPlayerId())
                    .orElseThrow(() -> new ResourceNotFoundException("SeasonTeamPlayer", req.potmPlayerId()));
            UUID potmTeamId = potm.getSeasonTeam().getId();
            if (!potmTeamId.equals(match.getHomeTeam().getId())
                    && !potmTeamId.equals(match.getAwayTeam().getId())) {
                throw new AppException("POTM player must belong to one of the two teams playing this match",
                        HttpStatus.BAD_REQUEST);
            }
        }

        String publishedBy = SecurityContextHolder.getContext().getAuthentication().getName();

        MatchResult result = MatchResult.builder()
                .match(match)
                .tossWinnerTeam(tossWinner)
                .matchWinnerTeam(matchWinner)
                .potmPlayer(potm)
                .tie(req.tie())
                .resultNotes(req.resultNotes())
                .publishedAt(Instant.now())
                .publishedBy(publishedBy)
                .build();
        result = matchResultRepository.save(result);

        // Update match status
        match.setStatus(Match.MatchStatus.COMPLETED);
        matchRepository.save(match);

        // Trigger async score calculation only after this transaction commits,
        // so the result row is visible to the async thread.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                scoreCalculationService.calculateScoresAsync(matchId);
            }
        });

        log.info("Match result published for matchId={} by {}", matchId, publishedBy);
        return MatchResultResponse.from(result);
    }

    /** Fetch the published result for a match, or throw if not yet published. */
    public MatchResultResponse getResult(UUID matchId) {
        return MatchResultResponse.from(
                matchResultRepository.findByMatch_Id(matchId)
                        .orElseThrow(() -> new ResourceNotFoundException("Result not yet published for match", matchId)));
    }

    /** Load a non-deleted match or throw {@link com.familyleague.common.exception.ResourceNotFoundException}. */
    public Match findOrThrow(UUID id) {
        return matchRepository.findById(id)
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Match", id));
    }

    /** Guard: throws 422 if the season is CLOSED. Used before any write operation. */
    private void requireSeasonOpen(Season season) {
        if (season.getStatus() == Season.SeasonStatus.CLOSED) {
            throw new AppException(
                "Season '" + season.getName() + "' is closed. No changes are allowed.",
                HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private SeasonTeam findSeasonTeamOrThrow(UUID id) {
        return seasonTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SeasonTeam", id));
    }
}

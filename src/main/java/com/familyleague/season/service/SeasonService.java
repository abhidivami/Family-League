package com.familyleague.season.service;

import com.familyleague.common.exception.AppException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.league.entity.League;
import com.familyleague.league.repository.LeagueRepository;
import com.familyleague.leaderboard.service.ScoreCalculationService;
import com.familyleague.season.dto.SeasonRequest;
import com.familyleague.season.dto.SeasonResponse;
import com.familyleague.season.entity.Season;
import com.familyleague.season.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeasonService {

    private final SeasonRepository seasonRepository;
    private final LeagueRepository leagueRepository;
    private final ScoreCalculationService scoreCalculationService;

    @Transactional
    public SeasonResponse create(UUID leagueId, SeasonRequest req) {
        League league = leagueRepository.findById(leagueId)
                .filter(l -> !l.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("League", leagueId));

        Season season = Season.builder()
                .league(league)
                .name(req.name())
                .year(req.year().shortValue())
                .status(Season.SeasonStatus.CREATED)
                .matchLockHours(req.matchLockHours() != null ? req.matchLockHours() : 1)
                .leagueLockHours(req.leagueLockHours() != null ? req.leagueLockHours() : 4)
                .build();

        return SeasonResponse.from(seasonRepository.save(season));
    }

    public Page<SeasonResponse> listByLeague(UUID leagueId, Pageable pageable) {
        return seasonRepository.findByLeague_IdAndDeletedAtIsNull(leagueId, pageable)
                .map(SeasonResponse::from);
    }

    public SeasonResponse getById(UUID id) {
        return SeasonResponse.from(findOrThrow(id));
    }

    @Transactional
    public SeasonResponse activate(UUID id) {
        Season season = findOrThrow(id);
        if (season.getStatus() != Season.SeasonStatus.CREATED) {
            throw new AppException("Season can only be activated from CREATED state", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        season.setStatus(Season.SeasonStatus.ACTIVE);
        return SeasonResponse.from(seasonRepository.save(season));
    }

    @Transactional
    public SeasonResponse close(UUID id) {
        Season season = findOrThrow(id);
        if (season.getStatus() != Season.SeasonStatus.ACTIVE) {
            throw new AppException("Only ACTIVE seasons can be closed", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        season.setStatus(Season.SeasonStatus.CLOSED);
        SeasonResponse response = SeasonResponse.from(seasonRepository.save(season));
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                scoreCalculationService.calculateSeasonScoresAsync(id);
            }
        });
        return response;
    }

    public Season findOrThrow(UUID id) {
        return seasonRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Season", id));
    }
}

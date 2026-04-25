package com.familyleague.team.service;

import com.familyleague.common.exception.AppException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.season.dto.SeasonTeamResponse;
import com.familyleague.season.entity.Season;
import com.familyleague.season.entity.SeasonTeam;
import com.familyleague.player.repository.PlayerRepository;
import com.familyleague.season.entity.SeasonTeamPlayer;
import com.familyleague.season.repository.SeasonTeamPlayerRepository;
import com.familyleague.season.repository.SeasonTeamRepository;
import com.familyleague.season.service.SeasonService;
import com.familyleague.team.dto.TeamRequest;
import com.familyleague.team.dto.TeamResponse;
import com.familyleague.team.entity.Team;
import com.familyleague.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * Manages {@link com.familyleague.team.entity.Team} records and their registration in seasons.
 *
 * <p>A Team is a global entity (e.g., "Mumbai Indians") that can participate in
 * multiple seasons via {@link com.familyleague.season.entity.SeasonTeam} join records.
 * Team names are unique (case-insensitive) across active (non-deleted) records.
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    private static final int MAX_SQUAD_SIZE = 16;

    private final TeamRepository teamRepository;
    private final SeasonTeamRepository seasonTeamRepository;
    private final SeasonTeamPlayerRepository seasonTeamPlayerRepository;
    private final PlayerRepository playerRepository;
    private final SeasonService seasonService;

    /**
     * Create a new team. Rejects duplicate names (case-insensitive) with 409.
     */
    @Transactional
    public TeamResponse createTeam(TeamRequest req) {
        if (teamRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(req.name())) {
            throw new AppException("A team named '" + req.name() + "' already exists", HttpStatus.CONFLICT);
        }
        Team team = Team.builder()
                .name(req.name())
                .shortName(req.shortName())
                .homeCity(req.homeCity())
                .logoUrl(req.logoUrl())
                .active(true)
                .build();
        return TeamResponse.from(teamRepository.save(team));
    }

    /** Search teams by name (case-insensitive) or list all active teams if search is blank. */
    public Page<TeamResponse> listTeams(String search, Pageable pageable) {
        if (StringUtils.hasText(search)) {
            return teamRepository
                    .findByNameContainingIgnoreCaseAndDeletedAtIsNull(search, pageable)
                    .map(TeamResponse::from);
        }
        return teamRepository.findByDeletedAtIsNull(pageable).map(TeamResponse::from);
    }

    public TeamResponse getTeamById(UUID id) {
        return TeamResponse.from(findTeamOrThrow(id));
    }

    /** Register a team for a season. */
    @Transactional
    public SeasonTeamResponse addTeamToSeason(UUID seasonId, UUID teamId) {
        Season season = seasonService.findOrThrow(seasonId);
        Team team = findTeamOrThrow(teamId);

        if (seasonTeamRepository.existsBySeason_IdAndTeam_Id(seasonId, teamId)) {
            throw new AppException("Team already added to this season", HttpStatus.CONFLICT);
        }

        SeasonTeam st = SeasonTeam.builder().season(season).team(team).build();
        st = seasonTeamRepository.save(st);

        return new SeasonTeamResponse(st.getId(), seasonId, teamId,
                team.getName(), team.getShortName());
    }

    public List<SeasonTeamResponse> listTeamsInSeason(UUID seasonId) {
        return seasonTeamRepository.findBySeason_Id(seasonId).stream()
                .map(st -> new SeasonTeamResponse(st.getId(), seasonId,
                        st.getTeam().getId(), st.getTeam().getName(), st.getTeam().getShortName()))
                .toList();
    }

    /**
     * Add a player to a season-team's squad (max 16 players).
     */
    @Transactional
    public void addPlayerToSquad(UUID seasonTeamId, UUID playerId, Short jerseyNumber) {
        SeasonTeam seasonTeam = seasonTeamRepository.findById(seasonTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("SeasonTeam", seasonTeamId));

        com.familyleague.player.entity.Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", playerId));

        long currentSquadSize = seasonTeamPlayerRepository.countBySeasonTeam_IdAndActiveTrue(seasonTeamId);
        if (currentSquadSize >= MAX_SQUAD_SIZE) {
            throw new AppException("Squad is full (max " + MAX_SQUAD_SIZE + " players)", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        SeasonTeamPlayer stp = SeasonTeamPlayer.builder()
                .seasonTeam(seasonTeam)
                .player(player)
                .jerseyNumber(jerseyNumber)
                .active(true)
                .build();
        seasonTeamPlayerRepository.save(stp);
    }

    private Team findTeamOrThrow(UUID id) {
        return teamRepository.findById(id)
                .filter(t -> !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }
}

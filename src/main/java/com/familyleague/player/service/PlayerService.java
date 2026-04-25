package com.familyleague.player.service;

import com.familyleague.common.exception.AppException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.player.dto.PlayerRequest;
import com.familyleague.player.dto.PlayerResponse;
import com.familyleague.player.dto.SquadPlayerResponse;
import com.familyleague.player.entity.Player;
import com.familyleague.player.repository.PlayerRepository;
import com.familyleague.season.entity.SeasonTeam;
import com.familyleague.season.entity.SeasonTeamPlayer;
import com.familyleague.season.repository.SeasonTeamPlayerRepository;
import com.familyleague.season.repository.SeasonTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private static final int MAX_SQUAD_SIZE = 16;

    private final PlayerRepository playerRepository;
    private final SeasonTeamPlayerRepository seasonTeamPlayerRepository;
    private final SeasonTeamRepository seasonTeamRepository;

    @Transactional
    public PlayerResponse createPlayer(PlayerRequest req) {
        Player player = Player.builder()
                .name(req.name())
                .dateOfBirth(req.dateOfBirth())
                .nationality(req.nationality())
                .battingStyle(req.battingStyle())
                .bowlingStyle(req.bowlingStyle())
                .playerRole(req.playerRole())
                .active(true)
                .build();
        return PlayerResponse.from(playerRepository.save(player));
    }

    public Page<PlayerResponse> listPlayers(String search, Pageable pageable) {
        if (StringUtils.hasText(search)) {
            return playerRepository
                    .findByNameContainingIgnoreCaseAndDeletedAtIsNull(search, pageable)
                    .map(PlayerResponse::from);
        }
        return playerRepository.findByDeletedAtIsNull(pageable).map(PlayerResponse::from);
    }

    public PlayerResponse getPlayerById(UUID id) {
        return PlayerResponse.from(findOrThrow(id));
    }

    /**
     * Add a player to a season-team's squad.
     * Squad limit is 16 players.
     * A player can only be in one team per season (enforced by unique constraint on player_id + season scope).
     */
    @Transactional
    public SquadPlayerResponse addPlayerToSquad(UUID seasonTeamId, UUID playerId, Short jerseyNumber) {
        SeasonTeam seasonTeam = seasonTeamRepository.findById(seasonTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("SeasonTeam", seasonTeamId));

        Player player = findOrThrow(playerId);

        long current = seasonTeamPlayerRepository.countBySeasonTeam_IdAndActiveTrue(seasonTeamId);
        if (current >= MAX_SQUAD_SIZE) {
            throw new AppException("Squad full: max " + MAX_SQUAD_SIZE + " players allowed", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (seasonTeamPlayerRepository.findBySeasonTeam_IdAndPlayer_Id(seasonTeamId, playerId).isPresent()) {
            throw new AppException("Player already in squad", HttpStatus.CONFLICT);
        }

        SeasonTeamPlayer stp = SeasonTeamPlayer.builder()
                .seasonTeam(seasonTeam)
                .player(player)
                .jerseyNumber(jerseyNumber)
                .active(true)
                .build();
        stp = seasonTeamPlayerRepository.save(stp);

        return new SquadPlayerResponse(stp.getId(), playerId, player.getName(),
                player.getPlayerRole().name(), jerseyNumber, true);
    }

    /** List the full squad for a season-team. */
    public List<SquadPlayerResponse> getSquad(UUID seasonTeamId) {
        return seasonTeamPlayerRepository.findBySeasonTeam_Id(seasonTeamId).stream()
                .map(stp -> new SquadPlayerResponse(
                        stp.getId(), stp.getPlayer().getId(), stp.getPlayer().getName(),
                        stp.getPlayer().getPlayerRole().name(), stp.getJerseyNumber(), stp.isActive()))
                .toList();
    }

    private Player findOrThrow(UUID id) {
        return playerRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Player", id));
    }
}

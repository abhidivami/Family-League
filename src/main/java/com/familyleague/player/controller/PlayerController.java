package com.familyleague.player.controller;

import com.familyleague.common.response.ApiResponse;
import com.familyleague.common.response.PagedResponse;
import com.familyleague.player.dto.PlayerHistoryEntry;
import com.familyleague.player.dto.PlayerRequest;
import com.familyleague.player.dto.PlayerResponse;
import com.familyleague.player.dto.SquadPlayerResponse;
import com.familyleague.player.service.PlayerService;
import com.familyleague.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Players", description = "Player master + squad management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final TeamService teamService;

    // ── Player master ────────────────────────────────────────

    @Operation(summary = "Create a player (Admin)")
    @PostMapping("/api/admin/players")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlayerResponse>> create(@Valid @RequestBody PlayerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(playerService.createPlayer(req)));
    }

    @Operation(summary = "List players (search + paginate)")
    @GetMapping("/api/players")
    public ResponseEntity<ApiResponse<PagedResponse<PlayerResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                new PagedResponse<>(playerService.listPlayers(search,
                        PageRequest.of(page, size, Sort.by("name"))))));
    }

    @Operation(summary = "Get player by ID")
    @GetMapping("/api/players/{id}")
    public ResponseEntity<ApiResponse<PlayerResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(playerService.getPlayerById(id)));
    }

    @Operation(summary = "Get player's season-team history")
    @GetMapping("/api/players/{id}/history")
    public ResponseEntity<ApiResponse<List<PlayerHistoryEntry>>> getHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(playerService.getPlayerHistory(id)));
    }

    // ── Squad management ─────────────────────────────────────

    /**
     * Add a player to a season-team squad.
     * Request: /api/admin/season-teams/{seasonTeamId}/players
     */
    @Operation(summary = "Add player to season-team squad (Admin, max 16)")
    @PostMapping("/api/admin/season-teams/{seasonTeamId}/players")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SquadPlayerResponse>> addToSquad(
            @PathVariable UUID seasonTeamId,
            @RequestParam UUID playerId,
            @RequestParam(required = false) Short jerseyNumber) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        playerService.addPlayerToSquad(seasonTeamId, playerId, jerseyNumber)));
    }

    @Operation(summary = "Get squad for a season-team")
    @GetMapping("/api/season-teams/{seasonTeamId}/squad")
    public ResponseEntity<ApiResponse<List<SquadPlayerResponse>>> getSquad(
            @PathVariable UUID seasonTeamId) {
        return ResponseEntity.ok(ApiResponse.ok(playerService.getSquad(seasonTeamId)));
    }
}

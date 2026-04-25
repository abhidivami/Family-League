package com.familyleague.team.controller;

import com.familyleague.common.response.ApiResponse;
import com.familyleague.common.response.PagedResponse;
import com.familyleague.season.dto.SeasonTeamResponse;
import com.familyleague.team.dto.TeamRequest;
import com.familyleague.team.dto.TeamResponse;
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

@Tag(name = "Teams", description = "Team master + season-team management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Create a team (Admin)")
    @PostMapping("/api/admin/teams")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeamResponse>> create(@Valid @RequestBody TeamRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(teamService.createTeam(req)));
    }

    @Operation(summary = "List teams")
    @GetMapping("/api/teams")
    public ResponseEntity<ApiResponse<PagedResponse<TeamResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                new PagedResponse<>(teamService.listTeams(search,
                        PageRequest.of(page, size, Sort.by("name"))))));
    }

    @Operation(summary = "Get team by ID")
    @GetMapping("/api/teams/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.getTeamById(id)));
    }

    @Operation(summary = "Add a team to a season (Admin)")
    @PostMapping("/api/admin/seasons/{seasonId}/teams/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SeasonTeamResponse>> addToSeason(
            @PathVariable UUID seasonId, @PathVariable UUID teamId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(teamService.addTeamToSeason(seasonId, teamId)));
    }

    @Operation(summary = "List teams participating in a season")
    @GetMapping("/api/seasons/{seasonId}/teams")
    public ResponseEntity<ApiResponse<List<SeasonTeamResponse>>> listInSeason(
            @PathVariable UUID seasonId) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.listTeamsInSeason(seasonId)));
    }
}

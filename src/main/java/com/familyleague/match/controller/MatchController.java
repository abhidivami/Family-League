package com.familyleague.match.controller;

import com.familyleague.common.response.ApiResponse;
import com.familyleague.common.response.PagedResponse;
import com.familyleague.match.dto.*;
import com.familyleague.match.service.MatchService;
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

import java.util.UUID;

@Tag(name = "Matches", description = "Match schedule, squad, and result management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @Operation(summary = "Create a match in a season (Admin)")
    @PostMapping("/api/admin/seasons/{seasonId}/matches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MatchResponse>> create(
            @PathVariable UUID seasonId, @Valid @RequestBody MatchRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(matchService.createMatch(seasonId, req)));
    }

    @Operation(summary = "List matches in a season")
    @GetMapping("/api/seasons/{seasonId}/matches")
    public ResponseEntity<ApiResponse<PagedResponse<MatchResponse>>> list(
            @PathVariable UUID seasonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                new PagedResponse<>(matchService.listBySeason(seasonId,
                        PageRequest.of(page, size, Sort.by("matchNumber"))))));
    }

    @Operation(summary = "Get match by ID")
    @GetMapping("/api/matches/{id}")
    public ResponseEntity<ApiResponse<MatchResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(matchService.getById(id)));
    }

    @Operation(summary = "Set Playing XI for a match (Admin)")
    @PatchMapping("/api/admin/matches/{matchId}/squad/{seasonTeamPlayerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> setPlayingXi(
            @PathVariable UUID matchId,
            @PathVariable UUID seasonTeamPlayerId,
            @RequestParam boolean playingXi) {
        matchService.setPlayingXi(matchId, seasonTeamPlayerId, playingXi);
        return ResponseEntity.ok(ApiResponse.ok("Squad updated", null));
    }

    @Operation(summary = "Publish match result (Admin) — triggers score recalculation")
    @PostMapping("/api/admin/matches/{matchId}/result")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MatchResultResponse>> publishResult(
            @PathVariable UUID matchId, @Valid @RequestBody MatchResultRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(matchService.publishResult(matchId, req)));
    }

    @Operation(summary = "Get result for a match")
    @GetMapping("/api/matches/{matchId}/result")
    public ResponseEntity<ApiResponse<MatchResultResponse>> getResult(@PathVariable UUID matchId) {
        return ResponseEntity.ok(ApiResponse.ok(matchService.getResult(matchId)));
    }
}

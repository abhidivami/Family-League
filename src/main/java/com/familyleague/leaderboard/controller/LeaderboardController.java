package com.familyleague.leaderboard.controller;

import com.familyleague.common.response.ApiResponse;
import com.familyleague.leaderboard.dto.TeamStandingEntry;
import com.familyleague.leaderboard.dto.UserLeaderboardEntry;
import com.familyleague.leaderboard.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Leaderboard endpoints.
 *
 * GET /api/leaderboard/seasons/{seasonId}/users
 *   Optional: ?afterMatch=5  → standings after 5 matches
 *   Omit afterMatch          → full season standings
 *
 * GET /api/leaderboard/seasons/{seasonId}/points-table
 *   → IPL-style team points table for the season
 */
@Tag(name = "Leaderboard", description = "User prediction leaderboard and IPL team points table")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/leaderboard/seasons/{seasonId}")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @Operation(summary = "User prediction leaderboard",
               description = "Pass `afterMatch` to see standings after N matches; omit for full season")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserLeaderboardEntry>>> getUserLeaderboard(
            @PathVariable UUID seasonId,
            @Parameter(description = "Show standings after this many matches (1-based)")
            @RequestParam(required = false) Short afterMatch) {
        return ResponseEntity.ok(ApiResponse.ok(
                leaderboardService.getUserLeaderboard(seasonId, afterMatch)));
    }

    @Operation(summary = "IPL-style team points table for the season")
    @GetMapping("/points-table")
    public ResponseEntity<ApiResponse<List<TeamStandingEntry>>> getPointsTable(
            @PathVariable UUID seasonId) {
        return ResponseEntity.ok(ApiResponse.ok(leaderboardService.getPointsTable(seasonId)));
    }
}

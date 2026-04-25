package com.familyleague.prediction.controller;

import com.familyleague.common.response.ApiResponse;
import com.familyleague.prediction.dto.SeasonPredictionRequest;
import com.familyleague.prediction.dto.SeasonPredictionResponse;
import com.familyleague.prediction.service.SeasonPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Season-level prediction endpoints.
 *
 * Window: open from season creation → (first match scheduledAt − leagueLockHours).
 * Users predict the final standings order of all teams in the season.
 */
@Tag(name = "Season Predictions", description = "Submit and view season standing predictions")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/season-predictions")
@RequiredArgsConstructor
public class SeasonPredictionController {

    private final SeasonPredictionService seasonPredictionService;

    @Operation(summary = "Submit or update your season standings prediction (before lock)")
    @PostMapping
    public ResponseEntity<ApiResponse<SeasonPredictionResponse>> submitOrUpdate(
            @Valid @RequestBody SeasonPredictionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(seasonPredictionService.submitOrUpdate(req)));
    }

    @Operation(summary = "Get my season standings prediction")
    @GetMapping("/seasons/{seasonId}/mine")
    public ResponseEntity<ApiResponse<SeasonPredictionResponse>> getMyPrediction(
            @PathVariable UUID seasonId) {
        return ResponseEntity.ok(ApiResponse.ok(seasonPredictionService.getMyPrediction(seasonId)));
    }

    @Operation(summary = "Get all season predictions (visible after lock; admin sees anytime)")
    @GetMapping("/seasons/{seasonId}/all")
    public ResponseEntity<ApiResponse<List<SeasonPredictionResponse>>> getAllPredictions(
            @PathVariable UUID seasonId,
            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(ApiResponse.ok(
            seasonPredictionService.getAllPredictions(seasonId, isAdmin)));
    }
}

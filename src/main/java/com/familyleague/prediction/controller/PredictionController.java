package com.familyleague.prediction.controller;

import com.familyleague.common.response.ApiResponse;
import com.familyleague.prediction.dto.PredictionRequest;
import com.familyleague.prediction.dto.PredictionResponse;
import com.familyleague.prediction.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * User-facing prediction endpoints.
 */
@Tag(name = "Predictions", description = "Submit and view match predictions")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @Operation(summary = "Submit or update prediction for a match (before lock)")
    @PostMapping("/matches")
    public ResponseEntity<ApiResponse<PredictionResponse>> submitOrUpdate(
            @Valid @RequestBody PredictionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(predictionService.submitOrUpdate(req)));
    }

    @Operation(summary = "Get my prediction for a match")
    @GetMapping("/matches/{matchId}/mine")
    public ResponseEntity<ApiResponse<PredictionResponse>> getMyPrediction(
            @PathVariable UUID matchId) {
        return ResponseEntity.ok(ApiResponse.ok(predictionService.getMyPrediction(matchId)));
    }

    @Operation(summary = "Get all predictions for a match (available only after lock)")
    @GetMapping("/matches/{matchId}/all")
    public ResponseEntity<ApiResponse<List<PredictionResponse>>> getAllForMatch(
            @PathVariable UUID matchId) {
        return ResponseEntity.ok(ApiResponse.ok(predictionService.getAllPredictionsForMatch(matchId)));
    }
}

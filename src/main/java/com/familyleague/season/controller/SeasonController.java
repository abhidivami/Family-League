package com.familyleague.season.controller;

import com.familyleague.common.response.ApiResponse;
import com.familyleague.common.response.PagedResponse;
import com.familyleague.season.dto.SeasonRequest;
import com.familyleague.season.dto.SeasonResponse;
import com.familyleague.season.service.SeasonService;
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

@Tag(name = "Seasons", description = "Season management per league")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonService seasonService;

    @Operation(summary = "Create a season under a league (Admin)")
    @PostMapping("/api/admin/leagues/{leagueId}/seasons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SeasonResponse>> create(
            @PathVariable UUID leagueId, @Valid @RequestBody SeasonRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(seasonService.create(leagueId, req)));
    }

    @Operation(summary = "List seasons for a league")
    @GetMapping("/api/leagues/{leagueId}/seasons")
    public ResponseEntity<ApiResponse<PagedResponse<SeasonResponse>>> list(
            @PathVariable UUID leagueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                new PagedResponse<>(seasonService.listByLeague(leagueId,
                        PageRequest.of(page, size, Sort.by("year").descending())))));
    }

    @Operation(summary = "Get season by ID")
    @GetMapping("/api/seasons/{id}")
    public ResponseEntity<ApiResponse<SeasonResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(seasonService.getById(id)));
    }

    @Operation(summary = "Activate a season (Admin)")
    @PatchMapping("/api/admin/seasons/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SeasonResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(seasonService.activate(id)));
    }

    @Operation(summary = "Close a season (Admin)")
    @PatchMapping("/api/admin/seasons/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SeasonResponse>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(seasonService.close(id)));
    }
}

package com.familyleague.league.controller;

import com.familyleague.common.response.ApiResponse;
import com.familyleague.common.response.PagedResponse;
import com.familyleague.league.dto.LeagueRequest;
import com.familyleague.league.dto.LeagueResponse;
import com.familyleague.league.service.LeagueService;
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

@Tag(name = "Leagues", description = "League management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;

    /** Admin: create a league */
    @Operation(summary = "Create a new league (Admin)")
    @PostMapping("/api/admin/leagues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LeagueResponse>> create(@Valid @RequestBody LeagueRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(leagueService.create(req)));
    }

    /** Admin: update a league */
    @Operation(summary = "Update a league (Admin)")
    @PutMapping("/api/admin/leagues/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LeagueResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody LeagueRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(leagueService.update(id, req)));
    }

    /** Admin: soft-delete a league */
    @Operation(summary = "Delete a league (Admin)")
    @DeleteMapping("/api/admin/leagues/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        leagueService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("League deleted", null));
    }

    /** All: list leagues with optional search + pagination */
    @Operation(summary = "List leagues")
    @GetMapping("/api/leagues")
    public ResponseEntity<ApiResponse<PagedResponse<LeagueResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort) {
        var pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(ApiResponse.ok(new PagedResponse<>(leagueService.list(search, pageable))));
    }

    /** All: get one league */
    @Operation(summary = "Get a league by ID")
    @GetMapping("/api/leagues/{id}")
    public ResponseEntity<ApiResponse<LeagueResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(leagueService.getById(id)));
    }
}

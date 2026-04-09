package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.stats.StatsResponse;
import com.project.soc.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Stats")
@SecurityRequirement(name = "bearerAuth")
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Dashboard stats overview")
    public ResponseEntity<ApiResponse<StatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.ok("Stats loaded", statsService.getStats()));
    }
}

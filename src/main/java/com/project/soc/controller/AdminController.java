package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.admin.AdminStatsResponse;
import com.project.soc.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aggregate record counts")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.ok("Admin stats", adminStatsService.stats()));
    }
}

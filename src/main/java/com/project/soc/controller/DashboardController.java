package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.dashboard.DashboardSummaryResponse;
import com.project.soc.dto.dashboard.RecentActivityItemDto;
import com.project.soc.dto.dashboard.RiskTrendPointDto;
import com.project.soc.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Dashboard summary for React UI")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> summary() {
        return ResponseEntity.ok(ApiResponse.ok("Dashboard summary", dashboardService.summary()));
    }

    @GetMapping("/risk-trend")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Risk trend time series")
    public ResponseEntity<ApiResponse<List<RiskTrendPointDto>>> riskTrend(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Risk trend", dashboardService.riskTrend(days)));
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Recent logs and alerts combined")
    public ResponseEntity<ApiResponse<List<RecentActivityItemDto>>> recentActivity() {
        return ResponseEntity.ok(ApiResponse.ok("Recent activity", dashboardService.recentActivity()));
    }
}

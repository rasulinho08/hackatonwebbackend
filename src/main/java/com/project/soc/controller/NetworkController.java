package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.dashboard.RecentActivityItemDto;
import com.project.soc.dto.network.NetworkTrafficDto;
import com.project.soc.dto.network.SystemHealthDto;
import com.project.soc.service.DashboardService;
import com.project.soc.service.NetworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Network & System")
@SecurityRequirement(name = "bearerAuth")
public class NetworkController {

    private final NetworkService networkService;
    private final DashboardService dashboardService;

    @GetMapping("/api/network/traffic")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Network traffic data (inbound/outbound Mbps)")
    public ResponseEntity<ApiResponse<List<NetworkTrafficDto>>> traffic() {
        return ResponseEntity.ok(ApiResponse.ok("Network traffic", networkService.getTraffic()));
    }

    @GetMapping("/api/system/health")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "System health: CPU, RAM, disk, sessions")
    public ResponseEntity<ApiResponse<SystemHealthDto>> health() {
        return ResponseEntity.ok(ApiResponse.ok("System health", networkService.getHealth()));
    }

    @GetMapping("/api/activity/feed")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Live activity stream")
    public ResponseEntity<ApiResponse<List<RecentActivityItemDto>>> feed() {
        return ResponseEntity.ok(ApiResponse.ok("Activity feed", dashboardService.recentActivity()));
    }
}

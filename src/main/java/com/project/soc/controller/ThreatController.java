package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.threat.*;
import com.project.soc.service.ThreatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/threats")
@RequiredArgsConstructor
@Tag(name = "Threat Intelligence")
@SecurityRequirement(name = "bearerAuth")
public class ThreatController {

    private final ThreatService threatService;

    @GetMapping("/ioc")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Indicators of Compromise list")
    public ResponseEntity<ApiResponse<List<ThreatIocResponse>>> ioc() {
        return ResponseEntity.ok(ApiResponse.ok("IOCs loaded", threatService.getIocs()));
    }

    @GetMapping("/top-attackers")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Top attacker IPs")
    public ResponseEntity<ApiResponse<List<TopAttackerDto>>> topAttackers() {
        return ResponseEntity.ok(ApiResponse.ok("Top attackers", threatService.getTopAttackers()));
    }

    @GetMapping("/distribution")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Threat type distribution for charts")
    public ResponseEntity<ApiResponse<List<ThreatDistributionDto>>> distribution() {
        return ResponseEntity.ok(ApiResponse.ok("Threat distribution", threatService.getDistribution()));
    }

    @GetMapping("/timeline")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Incident timeline")
    public ResponseEntity<ApiResponse<List<ThreatTimelineDto>>> timeline() {
        return ResponseEntity.ok(ApiResponse.ok("Threat timeline", threatService.getTimeline()));
    }

    @GetMapping("/map")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Geo threat map data")
    public ResponseEntity<ApiResponse<List<ThreatGeoDto>>> map() {
        return ResponseEntity.ok(ApiResponse.ok("Geo threat data", threatService.getGeoMap()));
    }
}

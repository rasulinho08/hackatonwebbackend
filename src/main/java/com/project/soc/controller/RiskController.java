package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.risk.RiskSummaryResponse;
import com.project.soc.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
@Tag(name = "Risk")
@SecurityRequirement(name = "bearerAuth")
public class RiskController {

    private final RiskService riskService;

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Current overall risk summary")
    public ResponseEntity<ApiResponse<RiskSummaryResponse>> current() {
        return ResponseEntity.ok(ApiResponse.ok("Current risk", riskService.current()));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Historical risk snapshots")
    public ResponseEntity<ApiResponse<List<RiskSummaryResponse>>> history(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Risk history", riskService.history(days)));
    }

    @PostMapping("/recalculate")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Recompute risk from current telemetry")
    public ResponseEntity<ApiResponse<RiskSummaryResponse>> recalculate() {
        return ResponseEntity.ok(ApiResponse.ok("Risk recalculated", riskService.recalculateAndPersist()));
    }
}

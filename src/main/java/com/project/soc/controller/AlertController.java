package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.PageData;
import com.project.soc.dto.alert.AlertResponse;
import com.project.soc.dto.alert.AssignAlertRequest;
import com.project.soc.dto.alert.CreateAlertRequest;
import com.project.soc.dto.alert.OpenAlertCountResponse;
import com.project.soc.dto.alert.UpdateAlertStatusRequest;
import com.project.soc.enums.AlertStatus;
import com.project.soc.enums.Severity;
import com.project.soc.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts")
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "List alerts with filters")
    public ResponseEntity<ApiResponse<PageData<AlertResponse>>> list(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Long assignedTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AlertResponse> page = alertService.findPage(severity, status, assignedTo, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Alerts fetched successfully", PageData.from(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Alert details")
    public ResponseEntity<ApiResponse<AlertResponse>> one(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Alert details", alertService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Create manual alert")
    public ResponseEntity<ApiResponse<AlertResponse>> create(@Valid @RequestBody CreateAlertRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Alert created", alertService.createManual(request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Update alert workflow status")
    public ResponseEntity<ApiResponse<AlertResponse>> status(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAlertStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Alert status updated", alertService.updateStatus(id, request)));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Assign alert to analyst")
    public ResponseEntity<ApiResponse<AlertResponse>> assign(
            @PathVariable Long id,
            @Valid @RequestBody AssignAlertRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Alert assigned", alertService.assign(id, request)));
    }

    @GetMapping("/open/count")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Count open alerts")
    public ResponseEntity<ApiResponse<OpenAlertCountResponse>> openCount() {
        return ResponseEntity.ok(ApiResponse.ok("Open alert count", new OpenAlertCountResponse(alertService.countOpen())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Delete alert")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        alertService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Alert deleted", null));
    }

    @GetMapping("/by-hour")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Alerts grouped by hour (last 24h)")
    public ResponseEntity<ApiResponse<java.util.List<com.project.soc.dto.alert.AlertByHourDto>>> byHour() {
        return ResponseEntity.ok(ApiResponse.ok("Alerts by hour", alertService.alertsByHour()));
    }

    @PostMapping("/{id}/ai-triage")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "AI auto-triage for an alert")
    public ResponseEntity<ApiResponse<com.project.soc.dto.alert.AiTriageResponse>> aiTriage(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("AI triage complete", alertService.aiTriage(id)));
    }
}

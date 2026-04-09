package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.PageData;
import com.project.soc.dto.log.BulkLogsRequest;
import com.project.soc.dto.log.CreateLogRequest;
import com.project.soc.dto.log.LogResponse;
import com.project.soc.dto.log.UpdateLogStatusRequest;
import com.project.soc.enums.EventType;
import com.project.soc.enums.LogStatus;
import com.project.soc.enums.Severity;
import com.project.soc.enums.SourceType;
import com.project.soc.service.LogService;
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

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Security logs")
@SecurityRequirement(name = "bearerAuth")
public class SecurityLogController {

    private final LogService logService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Ingest a single security log")
    public ResponseEntity<ApiResponse<LogResponse>> create(@Valid @RequestBody CreateLogRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Log ingested", logService.ingest(request)));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Bulk ingest logs")
    public ResponseEntity<ApiResponse<List<LogResponse>>> bulk(@Valid @RequestBody BulkLogsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Logs ingested", logService.ingestBulk(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Search and page security logs")
    public ResponseEntity<ApiResponse<PageData<LogResponse>>> list(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) SourceType sourceType,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) LogStatus status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<LogResponse> page = logService.findPage(severity, sourceType, eventType, status, from, to, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Logs fetched successfully", PageData.from(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Get log by id")
    public ResponseEntity<ApiResponse<LogResponse>> one(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Log details", logService.getById(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Update log triage status")
    public ResponseEntity<ApiResponse<LogResponse>> status(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLogStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Log status updated", logService.updateStatus(id, request)));
    }

    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Generate mock logs for demos")
    public ResponseEntity<ApiResponse<List<LogResponse>>> simulate(
            @RequestParam(defaultValue = "15") int count
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Mock logs created", logService.simulate(count)));
    }
}

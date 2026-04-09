package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.PageData;
import com.project.soc.dto.report.IncidentReportResponse;
import com.project.soc.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate/daily")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Generate daily threat report")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> daily() {
        return ResponseEntity.ok(ApiResponse.ok("Daily report generated", reportService.generateDaily()));
    }

    @PostMapping("/generate/incident/{alertId}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Generate incident report for alert")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> incident(@PathVariable Long alertId) {
        return ResponseEntity.ok(ApiResponse.ok("Incident report generated", reportService.generateForAlert(alertId)));
    }

    @PostMapping("/generate/phishing/{scanId}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Generate phishing narrative report")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> phishing(@PathVariable Long scanId) {
        return ResponseEntity.ok(ApiResponse.ok("Phishing report generated", reportService.generateForPhishing(scanId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "List reports")
    public ResponseEntity<ApiResponse<PageData<IncidentReportResponse>>> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<IncidentReportResponse> page = reportService.list(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Reports fetched", PageData.from(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Report by id")
    public ResponseEntity<ApiResponse<IncidentReportResponse>> one(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Report details", reportService.getById(id)));
    }

    @GetMapping(value = "/{id}/download", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Download report as plain text")
    public ResponseEntity<String> download(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.downloadText(id));
    }
}

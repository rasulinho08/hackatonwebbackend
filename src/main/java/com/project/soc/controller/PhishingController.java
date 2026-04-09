package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.PageData;
import com.project.soc.dto.phishing.PhishingAnalyzeRequest;
import com.project.soc.dto.phishing.PhishingAnalyzeResponse;
import com.project.soc.dto.phishing.PhishingScanResponse;
import com.project.soc.dto.phishing.QuarantinedEmailResponse;
import com.project.soc.service.PhishingService;
import com.project.soc.service.QuarantineService;
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
@RequestMapping("/api/phishing")
@RequiredArgsConstructor
@Tag(name = "Phishing")
@SecurityRequirement(name = "bearerAuth")
public class PhishingController {

    private final PhishingService phishingService;
    private final QuarantineService quarantineService;

    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Analyze email content (rule-based MVP)")
    public ResponseEntity<ApiResponse<PhishingAnalyzeResponse>> analyze(@Valid @RequestBody PhishingAnalyzeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Analysis complete", phishingService.analyze(request)));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Past phishing scans")
    public ResponseEntity<ApiResponse<PageData<PhishingScanResponse>>> history(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PhishingScanResponse> page = phishingService.history(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Phishing history", PageData.from(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Single phishing scan")
    public ResponseEntity<ApiResponse<PhishingScanResponse>> one(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Phishing scan", phishingService.getById(id)));
    }

    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Run canned phishing sample")
    public ResponseEntity<ApiResponse<PhishingAnalyzeResponse>> simulate() {
        return ResponseEntity.ok(ApiResponse.ok("Simulated phishing analysis", phishingService.simulate()));
    }

    @GetMapping("/quarantine")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "List quarantined emails")
    public ResponseEntity<ApiResponse<java.util.List<QuarantinedEmailResponse>>> quarantine() {
        return ResponseEntity.ok(ApiResponse.ok("Quarantined emails", quarantineService.getQuarantined()));
    }

    @PostMapping("/quarantine/{id}/release")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Release quarantined email")
    public ResponseEntity<ApiResponse<QuarantinedEmailResponse>> release(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Email released", quarantineService.release(id)));
    }

    @DeleteMapping("/quarantine/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Delete quarantined email")
    public ResponseEntity<ApiResponse<Void>> deleteQuarantined(@PathVariable Long id) {
        quarantineService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Email deleted", null));
    }
}

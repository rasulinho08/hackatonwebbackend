package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.integration.IntegrationStatusResponse;
import com.project.soc.service.IntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
@Tag(name = "Integrations")
@SecurityRequirement(name = "bearerAuth")
public class IntegrationController {

    private final IntegrationService integrationService;

    @PostMapping("/wazuh/test")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Test Wazuh / SIEM connectivity (placeholder)")
    public ResponseEntity<ApiResponse<Map<String, String>>> testWazuh() {
        String result = integrationService.testWazuh();
        return ResponseEntity.ok(ApiResponse.ok("Wazuh test executed", Map.of("status", result)));
    }

    @PostMapping("/gemini/test")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "Test Gemini API key")
    public ResponseEntity<ApiResponse<Map<String, String>>> testGemini() {
        String result = integrationService.testGemini();
        return ResponseEntity.ok(ApiResponse.ok("Gemini test executed", Map.of("status", result)));
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Integration health summary")
    public ResponseEntity<ApiResponse<IntegrationStatusResponse>> status() {
        return ResponseEntity.ok(ApiResponse.ok("Integration status", integrationService.status()));
    }
}

package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "System")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "API health check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> payload = Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.ok("Service is running", payload));
    }
}

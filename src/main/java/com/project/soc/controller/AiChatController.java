package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.ai.AiChatRequest;
import com.project.soc.dto.ai.AiChatResponse;
import com.project.soc.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chat")
@SecurityRequirement(name = "bearerAuth")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Operation(summary = "AI-powered threat hunting chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("AI response", aiChatService.chat(req.getMessage())));
    }
}

package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.auth.AuthResponse;
import com.project.soc.dto.auth.LoginRequest;
import com.project.soc.dto.auth.RegisterRequest;
import com.project.soc.dto.auth.UserResponse;
import com.project.soc.service.AuthService;
import com.project.soc.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse body = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Registered successfully", body));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse body = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", body));
    }

    @GetMapping("/me")
    @Operation(summary = "Current user profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        UserResponse profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.ok("Profile loaded", profile));
    }
}

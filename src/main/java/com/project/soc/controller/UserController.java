package com.project.soc.controller;

import com.project.soc.dto.ApiResponse;
import com.project.soc.dto.auth.AuditLogResponse;
import com.project.soc.dto.auth.UserResponse;
import com.project.soc.entity.User;
import com.project.soc.enums.Role;
import com.project.soc.exception.ResourceNotFoundException;
import com.project.soc.mapper.DomainMapper;
import com.project.soc.repository.UserRepository;
import com.project.soc.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users & Audit")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final DomainMapper domainMapper;
    private final AuditService auditService;

    @GetMapping("/api/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users (admin only)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> all() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(domainMapper::toUserResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok("Users loaded", users));
    }

    @GetMapping("/api/users/me")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication auth) {
        Long userId = Long.parseLong((String) auth.getPrincipal());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.ok("Profile", domainMapper.toUserResponse(user)));
    }

    @PatchMapping("/api/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role or status")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (body.containsKey("role")) {
            user.setRole(Role.valueOf(body.get("role")));
        }
        user = userRepository.save(user);
        Long adminId = Long.parseLong((String) auth.getPrincipal());
        auditService.log(adminId, null, "UPDATE_USER",
                "Updated user #" + id + " role to " + user.getRole(), null);
        return ResponseEntity.ok(ApiResponse.ok("User updated", domainMapper.toUserResponse(user)));
    }

    @PostMapping("/api/auth/logout")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(summary = "Logout (invalidate session reference)")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication auth) {
        Long userId = Long.parseLong((String) auth.getPrincipal());
        auditService.log(userId, null, "LOGOUT", "User logged out", null);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @GetMapping("/api/audit-log")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Audit trail")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> auditLog() {
        return ResponseEntity.ok(ApiResponse.ok("Audit log", auditService.getAll()));
    }
}

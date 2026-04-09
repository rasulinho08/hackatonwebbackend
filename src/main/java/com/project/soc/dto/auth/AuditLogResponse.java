package com.project.soc.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String action;
    private String details;
    private String ipAddress;
    private Instant createdAt;
}

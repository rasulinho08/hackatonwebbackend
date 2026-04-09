package com.project.soc.service;

import com.project.soc.dto.auth.AuditLogResponse;
import com.project.soc.entity.AuditLog;
import com.project.soc.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repo;

    @Transactional
    public void log(Long userId, String email, String action, String details, String ip) {
        AuditLog entry = AuditLog.builder()
                .userId(userId)
                .userEmail(email)
                .action(action)
                .details(details)
                .ipAddress(ip)
                .build();
        repo.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAll() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(a -> AuditLogResponse.builder()
                        .id(a.getId())
                        .userId(a.getUserId())
                        .userEmail(a.getUserEmail())
                        .action(a.getAction())
                        .details(a.getDetails())
                        .ipAddress(a.getIpAddress())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }
}

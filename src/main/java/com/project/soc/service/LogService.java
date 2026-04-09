package com.project.soc.service;

import com.project.soc.dto.log.BulkLogsRequest;
import com.project.soc.dto.log.CreateLogRequest;
import com.project.soc.dto.log.LogResponse;
import com.project.soc.dto.log.UpdateLogStatusRequest;
import com.project.soc.entity.SecurityLog;
import com.project.soc.enums.LogStatus;
import com.project.soc.exception.ResourceNotFoundException;
import com.project.soc.mapper.DomainMapper;
import com.project.soc.repository.SecurityLogRepository;
import com.project.soc.repository.spec.SecurityLogSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {

    private final SecurityLogRepository securityLogRepository;
    private final AlertService alertService;
    private final DomainMapper domainMapper;
    private final MockDataService mockDataService;
    private final RiskService riskService;

    @Transactional
    public LogResponse ingest(CreateLogRequest req) {
        SecurityLog log = mapToEntity(req);
        if (log.getOccurredAt() == null) {
            log.setOccurredAt(Instant.now());
        }
        if (log.getStatus() == null) {
            log.setStatus(LogStatus.NEW);
        }
        log = securityLogRepository.save(log);
        alertService.evaluateAutomationsForLog(log);
        riskService.recalculateAndPersist();
        return domainMapper.toLogResponse(log);
    }

    @Transactional
    public List<LogResponse> ingestBulk(BulkLogsRequest request) {
        List<LogResponse> out = new ArrayList<>();
        for (CreateLogRequest r : request.getLogs()) {
            SecurityLog log = mapToEntity(r);
            if (log.getOccurredAt() == null) {
                log.setOccurredAt(Instant.now());
            }
            if (log.getStatus() == null) {
                log.setStatus(LogStatus.NEW);
            }
            log = securityLogRepository.save(log);
            alertService.evaluateAutomationsForLog(log);
            out.add(domainMapper.toLogResponse(log));
        }
        riskService.recalculateAndPersist();
        return out;
    }

    @Transactional(readOnly = true)
    public Page<LogResponse> findPage(
            com.project.soc.enums.Severity severity,
            com.project.soc.enums.SourceType sourceType,
            com.project.soc.enums.EventType eventType,
            LogStatus status,
            Instant from,
            Instant to,
            String keyword,
            Pageable pageable
    ) {
        Specification<SecurityLog> spec = SecurityLogSpecifications.filter(
                severity, sourceType, eventType, status, from, to, keyword);
        return securityLogRepository.findAll(spec, pageable).map(domainMapper::toLogResponse);
    }

    @Transactional(readOnly = true)
    public LogResponse getById(Long id) {
        SecurityLog log = securityLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found"));
        return domainMapper.toLogResponse(log);
    }

    @Transactional
    public LogResponse updateStatus(Long id, UpdateLogStatusRequest req) {
        SecurityLog log = securityLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found"));
        log.setStatus(req.getStatus());
        log = securityLogRepository.save(log);
        riskService.recalculateAndPersist();
        return domainMapper.toLogResponse(log);
    }

    @Transactional
    public List<LogResponse> simulate(int count) {
        return mockDataService.generateMockLogs(Math.min(Math.max(count, 1), 100));
    }

    private static SecurityLog mapToEntity(CreateLogRequest r) {
        return SecurityLog.builder()
                .sourceType(r.getSourceType())
                .eventType(r.getEventType())
                .rawMessage(r.getRawMessage())
                .ipAddress(r.getIpAddress())
                .hostname(r.getHostname())
                .username(r.getUsername())
                .severity(r.getSeverity())
                .status(r.getStatus())
                .occurredAt(r.getOccurredAt())
                .riskScore(r.getRiskScore())
                .metadataJson(r.getMetadataJson())
                .build();
    }
}

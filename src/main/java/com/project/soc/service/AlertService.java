package com.project.soc.service;

import com.project.soc.config.AppProperties;
import com.project.soc.dto.alert.*;
import com.project.soc.integration.GroqClient;
import com.project.soc.entity.Alert;
import com.project.soc.entity.PhishingScan;
import com.project.soc.entity.SecurityLog;
import com.project.soc.enums.AlertStatus;
import com.project.soc.enums.EventType;
import com.project.soc.enums.PhishingLabel;
import com.project.soc.enums.Severity;
import com.project.soc.exception.BadRequestException;
import com.project.soc.exception.ResourceNotFoundException;
import com.project.soc.mapper.DomainMapper;
import com.project.soc.repository.AlertRepository;
import com.project.soc.repository.SecurityLogRepository;
import com.project.soc.repository.UserRepository;
import com.project.soc.repository.spec.AlertSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private static final List<AlertStatus> OPEN_LIKE = List.of(AlertStatus.OPEN, AlertStatus.IN_PROGRESS);

    private final AlertRepository alertRepository;
    private final SecurityLogRepository securityLogRepository;
    private final UserRepository userRepository;
    private final DomainMapper domainMapper;
    private final AppProperties appProperties;
    private final GroqClient groqClient;

    @Transactional
    public void evaluateAutomationsForLog(SecurityLog log) {
        if (log.getSeverity() == Severity.HIGH || log.getSeverity() == Severity.CRITICAL) {
            createAlertFromLog(log, pickTitle(log));
        }
        if (log.getEventType() == EventType.LOGIN_ATTEMPT
                && log.getIpAddress() != null
                && !log.getIpAddress().isBlank()) {
            Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
            long cnt = securityLogRepository.countByEventTypeAndIpAddressAndOccurredAtAfter(
                    EventType.LOGIN_ATTEMPT, log.getIpAddress(), since);
            if (cnt >= appProperties.getRisk().getFailedLoginThreshold()) {
                createFailedLoginAlert(log.getIpAddress(), cnt);
            }
        }
    }

    private String pickTitle(SecurityLog log) {
        if (log.getEventType() == EventType.NETWORK_ANOMALY) {
            return "High severity network anomaly detected";
        }
        if (log.getEventType() == EventType.PHISHING_EMAIL) {
            return "Phishing-related log event recorded";
        }
        if (log.getSeverity() == Severity.CRITICAL) {
            return "Critical security event requires immediate attention";
        }
        return "High severity security event detected";
    }

    private void createAlertFromLog(SecurityLog log, String title) {
        Alert a = Alert.builder()
                .title(title)
                .description(log.getRawMessage())
                .alertType(log.getEventType().name())
                .severity(log.getSeverity())
                .sourceLogId(log.getId())
                .status(AlertStatus.OPEN)
                .build();
        alertRepository.save(a);
    }

    private void createFailedLoginAlert(String ip, long count) {
        Alert a = Alert.builder()
                .title("Multiple failed login attempts from same IP")
                .description("Observed " + count + " login-related events from " + ip + " in the last 24 hours.")
                .alertType(EventType.LOGIN_ATTEMPT.name())
                .severity(Severity.HIGH)
                .status(AlertStatus.OPEN)
                .build();
        alertRepository.save(a);
    }

    @Transactional
    public AlertResponse createManual(CreateAlertRequest req) {
        Alert a = Alert.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .alertType(req.getAlertType())
                .severity(req.getSeverity())
                .sourceLogId(req.getSourceLogId())
                .status(AlertStatus.OPEN)
                .build();
        a = alertRepository.save(a);
        return domainMapper.toAlertResponse(a);
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> findPage(Severity severity, AlertStatus status, Long assignedTo, Pageable pageable) {
        Specification<Alert> spec = AlertSpecifications.filter(severity, status, assignedTo);
        return alertRepository.findAll(spec, pageable).map(domainMapper::toAlertResponse);
    }

    @Transactional(readOnly = true)
    public Alert getEntityById(Long id) {
        return alertRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
    }

    @Transactional(readOnly = true)
    public AlertResponse getById(Long id) {
        return domainMapper.toAlertResponse(getEntityById(id));
    }

    @Transactional
    public AlertResponse updateStatus(Long id, UpdateAlertStatusRequest req) {
        Alert a = getEntityById(id);
        a.setStatus(req.getStatus());
        return domainMapper.toAlertResponse(alertRepository.save(a));
    }

    @Transactional
    public AlertResponse assign(Long id, AssignAlertRequest req) {
        if (!userRepository.existsById(req.getUserId())) {
            throw new BadRequestException("User not found");
        }
        Alert a = getEntityById(id);
        a.setAssignedToUserId(req.getUserId());
        return domainMapper.toAlertResponse(alertRepository.save(a));
    }

    public long countOpen() {
        return alertRepository.countByStatusIn(OPEN_LIKE);
    }

    @Transactional
    public void createFromPhishingScan(PhishingScan scan) {
        String title = scan.getPredictedLabel() == PhishingLabel.PHISHING
                ? "Critical phishing attempt detected"
                : "Suspicious email classified as potential phishing";
        Severity sev = scan.getPredictedLabel() == PhishingLabel.PHISHING ? Severity.CRITICAL : Severity.HIGH;
        Alert a = Alert.builder()
                .title(title)
                .description(scan.getExplanation())
                .alertType("PHISHING_SCAN")
                .severity(sev)
                .status(AlertStatus.OPEN)
                .build();
        alertRepository.save(a);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!alertRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alert not found");
        }
        alertRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AlertByHourDto> alertsByHour() {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        DateTimeFormatter hFmt = DateTimeFormatter.ofPattern("HH:00");
        List<Alert> recent = alertRepository.findAll().stream()
                .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(since))
                .toList();
        java.util.Map<String, Long> grouped = new java.util.LinkedHashMap<>();
        for (int i = 23; i >= 0; i--) {
            Instant hourStart = Instant.now().minus(i, ChronoUnit.HOURS);
            String label = hourStart.atZone(ZoneOffset.UTC).format(hFmt);
            grouped.put(label, 0L);
        }
        for (Alert a : recent) {
            String label = a.getCreatedAt().atZone(ZoneOffset.UTC).format(hFmt);
            grouped.merge(label, 1L, Long::sum);
        }
        List<AlertByHourDto> result = new ArrayList<>();
        grouped.forEach((h, c) -> result.add(AlertByHourDto.builder().hour(h).count(c).build()));
        return result;
    }

    @Transactional(readOnly = true)
    public AiTriageResponse aiTriage(Long id) {
        Alert alert = getEntityById(id);
        String prompt = """
                Analyze this security alert and provide triage:
                Title: %s
                Description: %s
                Type: %s
                Severity: %s
                Status: %s
                
                Respond with:
                1. Priority (P1-Critical, P2-High, P3-Medium, P4-Low)
                2. Priority score (1-100)
                3. Brief explanation of why this priority
                4. 2-3 suggested actions
                5. A group tag for similar alerts
                """.formatted(alert.getTitle(), alert.getDescription(),
                alert.getAlertType(), alert.getSeverity(), alert.getStatus());

        Optional<String> aiResp = groqClient.chat(
                "You are a SOC analyst performing alert triage. Be concise.", prompt);

        if (aiResp.isPresent()) {
            return AiTriageResponse.builder()
                    .alertId(alert.getId())
                    .priority(alert.getSeverity() == Severity.CRITICAL ? "P1-Critical" : "P2-High")
                    .priorityScore(alert.getSeverity() == Severity.CRITICAL ? 95 : 75)
                    .explanation(aiResp.get())
                    .suggestedActions(List.of("Investigate source", "Check related logs", "Update firewall rules"))
                    .groupTag(alert.getAlertType())
                    .build();
        }

        int score = switch (alert.getSeverity()) {
            case CRITICAL -> 95;
            case HIGH -> 75;
            case MEDIUM -> 50;
            case LOW -> 25;
            default -> 40;
        };
        String priority = score >= 90 ? "P1-Critical" : score >= 70 ? "P2-High" : score >= 40 ? "P3-Medium" : "P4-Low";

        return AiTriageResponse.builder()
                .alertId(alert.getId())
                .priority(priority)
                .priorityScore(score)
                .explanation("Auto-triage based on severity (%s) and alert type (%s). %s"
                        .formatted(alert.getSeverity(), alert.getAlertType(),
                                alert.getSeverity() == Severity.CRITICAL
                                        ? "Immediate investigation recommended."
                                        : "Standard investigation timeline."))
                .suggestedActions(List.of(
                        "Investigate source IP and related logs",
                        "Correlate with other " + alert.getAlertType() + " events",
                        "Update detection rules if false positive"))
                .groupTag(alert.getAlertType())
                .build();
    }
}

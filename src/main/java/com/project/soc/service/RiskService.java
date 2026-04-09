package com.project.soc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.soc.dto.dashboard.RiskTrendPointDto;
import com.project.soc.dto.risk.RiskSummaryResponse;
import com.project.soc.entity.RiskSnapshot;
import com.project.soc.enums.AlertStatus;
import com.project.soc.enums.LogStatus;
import com.project.soc.enums.PhishingLabel;
import com.project.soc.enums.Severity;
import com.project.soc.repository.AlertRepository;
import com.project.soc.repository.PhishingScanRepository;
import com.project.soc.repository.RiskSnapshotRepository;
import com.project.soc.repository.SecurityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskService {

    private static final List<LogStatus> RESOLVED_OR_FP = List.of(LogStatus.RESOLVED, LogStatus.FALSE_POSITIVE);
    private static final List<AlertStatus> OPEN_LIKE = List.of(AlertStatus.OPEN, AlertStatus.IN_PROGRESS);
    private static final List<Severity> HIGH_CRITICAL = List.of(Severity.HIGH, Severity.CRITICAL);
    private static final List<PhishingLabel> PHISH_LABELS = List.of(PhishingLabel.PHISHING, PhishingLabel.SUSPICIOUS);

    private final SecurityLogRepository securityLogRepository;
    private final AlertRepository alertRepository;
    private final PhishingScanRepository phishingScanRepository;
    private final RiskSnapshotRepository riskSnapshotRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public RiskSummaryResponse recalculateAndPersist() {
        List<String> explanations = new ArrayList<>();
        int score = 0;

        long criticalLogs = securityLogRepository.countBySeverityAndStatusNotIn(Severity.CRITICAL, RESOLVED_OR_FP);
        int critContribution = (int) Math.min(40, criticalLogs * 10);
        score += critContribution;
        if (criticalLogs > 0) {
            explanations.add("High number of unresolved critical-severity logs.");
        }

        long highLogs = securityLogRepository.countBySeverityAndStatusNotIn(Severity.HIGH, RESOLVED_OR_FP);
        score += (int) Math.min(30, highLogs * 6);
        if (highLogs > 0) {
            explanations.add("Elevated volume of high-severity log events.");
        }

        long medLogs = securityLogRepository.countBySeverityAndStatusNotIn(Severity.MEDIUM, RESOLVED_OR_FP);
        score += (int) Math.min(20, medLogs * 3);

        long lowLogs = securityLogRepository.countBySeverityAndStatusNotIn(Severity.LOW, RESOLVED_OR_FP);
        score += (int) Math.min(10, lowLogs * 1);

        long openCriticalAlerts = alertRepository.countBySeverityAndStatusIn(Severity.CRITICAL, OPEN_LIKE);
        int alertCrit = (int) Math.min(30, openCriticalAlerts * 15);
        score += alertCrit;
        if (openCriticalAlerts > 0) {
            explanations.add("Unresolved critical alerts are driving risk higher.");
        }

        Instant dayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        long recentPhish = phishingScanRepository.countByPredictedLabelInAndCreatedAtAfter(PHISH_LABELS, dayAgo);
        if (recentPhish > 0) {
            score += 20;
            explanations.add("Recent phishing detections increased system risk.");
        }

        score = Math.min(100, score);
        if (explanations.isEmpty()) {
            explanations.add("No major automated risk drivers detected in the current window.");
        }

        int activeThreatCount = (int) alertRepository.countBySeverityInAndStatusIn(HIGH_CRITICAL, OPEN_LIKE);
        int criticalAlertCount = (int) openCriticalAlerts;
        long phishingDetectedCount = phishingScanRepository.countByPredictedLabelAndCreatedAtAfter(
                PhishingLabel.PHISHING, Instant.now().minus(30, ChronoUnit.DAYS));

        int safePct = Math.max(0, 100 - Math.min(100, score));
        BigDecimal safe = BigDecimal.valueOf(safePct).setScale(2, RoundingMode.HALF_UP);

        String explanationJson;
        try {
            explanationJson = objectMapper.writeValueAsString(explanations);
        } catch (JsonProcessingException e) {
            explanationJson = "[]";
        }

        RiskSnapshot snap = RiskSnapshot.builder()
                .overallRiskScore(score)
                .activeThreatCount(activeThreatCount)
                .phishingDetectedCount((int) phishingDetectedCount)
                .criticalAlertCount(criticalAlertCount)
                .safeSystemsPercent(safe)
                .snapshotTime(Instant.now())
                .explanationJson(explanationJson)
                .build();
        riskSnapshotRepository.save(snap);

        return toSummary(snap, explanations);
    }

    @Transactional(readOnly = true)
    public RiskSummaryResponse current() {
        return riskSnapshotRepository.findFirstByOrderBySnapshotTimeDesc()
                .map(s -> toSummary(s, parseExplanations(s.getExplanationJson())))
                .orElseGet(RiskService::emptyRiskSummary);
    }

    private static RiskSummaryResponse emptyRiskSummary() {
        return RiskSummaryResponse.builder()
                .overallRiskScore(0)
                .activeThreatCount(0)
                .phishingDetectedCount(0)
                .criticalAlertCount(0)
                .safeSystemsPercent(100)
                .explanations(List.of("No snapshot yet. Ingest logs or call POST /api/risk/recalculate."))
                .build();
    }

    @Transactional(readOnly = true)
    public List<RiskSummaryResponse> history(int days) {
        Instant from = Instant.now().minus(Math.max(1, Math.min(days, 90)), ChronoUnit.DAYS);
        return riskSnapshotRepository.findBySnapshotTimeAfterOrderBySnapshotTimeAsc(from).stream()
                .map(s -> toSummary(s, parseExplanations(s.getExplanationJson())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RiskTrendPointDto> trend(int days) {
        int d = Math.max(1, Math.min(days, 90));
        Instant from = Instant.now().minus(d, ChronoUnit.DAYS);
        List<RiskSnapshot> snaps = riskSnapshotRepository.findBySnapshotTimeAfterOrderBySnapshotTimeAsc(from);
        if (snaps.isEmpty()) {
            RiskSummaryResponse cur = current();
            return List.of(new RiskTrendPointDto(
                    DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault()).format(Instant.now()),
                    cur.getOverallRiskScore()));
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());
        return snaps.stream()
                .map(s -> new RiskTrendPointDto(fmt.format(s.getSnapshotTime()), s.getOverallRiskScore()))
                .toList();
    }

    private static RiskSummaryResponse toSummary(RiskSnapshot s, List<String> explanations) {
        return RiskSummaryResponse.builder()
                .overallRiskScore(s.getOverallRiskScore())
                .activeThreatCount(s.getActiveThreatCount())
                .phishingDetectedCount(s.getPhishingDetectedCount())
                .criticalAlertCount(s.getCriticalAlertCount())
                .safeSystemsPercent(s.getSafeSystemsPercent().intValue())
                .explanations(explanations)
                .build();
    }

    private List<String> parseExplanations(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}

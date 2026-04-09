package com.project.soc.service;

import com.project.soc.dto.dashboard.DashboardSummaryResponse;
import com.project.soc.dto.dashboard.RecentActivityItemDto;
import com.project.soc.dto.dashboard.RecentAlertSummaryDto;
import com.project.soc.dto.dashboard.RiskTrendPointDto;
import com.project.soc.entity.Alert;
import com.project.soc.entity.SecurityLog;
import com.project.soc.enums.AlertStatus;
import com.project.soc.enums.Severity;
import com.project.soc.repository.AlertRepository;
import com.project.soc.repository.PhishingScanRepository;
import com.project.soc.repository.SecurityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final List<AlertStatus> OPEN_LIKE = List.of(AlertStatus.OPEN, AlertStatus.IN_PROGRESS);
    private static final List<Severity> HIGH_CRITICAL = List.of(Severity.HIGH, Severity.CRITICAL);

    private final AlertRepository alertRepository;
    private final SecurityLogRepository securityLogRepository;
    private final PhishingScanRepository phishingScanRepository;
    private final RiskService riskService;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        var risk = riskService.current();
        long totalAlerts = alertRepository.count();
        long activeThreats = alertRepository.countBySeverityInAndStatusIn(HIGH_CRITICAL, OPEN_LIKE);
        long phishingCount = phishingScanRepository.count();

        List<Alert> recent = alertRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
        List<RecentAlertSummaryDto> recentDtos = recent.stream()
                .map(a -> RecentAlertSummaryDto.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .severity(a.getSeverity())
                        .status(a.getStatus())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();

        List<RiskTrendPointDto> trend = riskService.trend(7);

        return DashboardSummaryResponse.builder()
                .totalAlerts(totalAlerts)
                .activeThreats(activeThreats)
                .riskScore(risk.getOverallRiskScore())
                .safeSystemsPercent(risk.getSafeSystemsPercent())
                .phishingDetectedCount(phishingCount)
                .recentAlerts(recentDtos)
                .riskTrend(trend)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RiskTrendPointDto> riskTrend(int days) {
        return riskService.trend(days);
    }

    @Transactional(readOnly = true)
    public List<RecentActivityItemDto> recentActivity() {
        List<SecurityLog> logs = securityLogRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "occurredAt"))).getContent();
        List<Alert> alerts = alertRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();

        List<RecentActivityItemDto> items = new ArrayList<>();
        for (SecurityLog l : logs) {
            items.add(RecentActivityItemDto.builder()
                    .type("LOG")
                    .id(l.getId())
                    .title(l.getEventType().name() + ": " + truncate(l.getRawMessage(), 80))
                    .severity(l.getSeverity())
                    .occurredAt(l.getOccurredAt())
                    .build());
        }
        for (Alert a : alerts) {
            items.add(RecentActivityItemDto.builder()
                    .type("ALERT")
                    .id(a.getId())
                    .title(a.getTitle())
                    .severity(a.getSeverity())
                    .occurredAt(a.getCreatedAt())
                    .build());
        }
        items.sort(Comparator.comparing(RecentActivityItemDto::getOccurredAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return items.stream().limit(15).toList();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}

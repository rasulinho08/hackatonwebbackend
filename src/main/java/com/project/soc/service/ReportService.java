package com.project.soc.service;

import com.project.soc.dto.report.IncidentReportResponse;
import com.project.soc.entity.Alert;
import com.project.soc.entity.IncidentReport;
import com.project.soc.entity.PhishingScan;
import com.project.soc.entity.SecurityLog;
import com.project.soc.enums.GeneratedBy;
import com.project.soc.enums.ReportType;
import com.project.soc.exception.ResourceNotFoundException;
import com.project.soc.mapper.DomainMapper;
import com.project.soc.repository.AlertRepository;
import com.project.soc.repository.IncidentReportRepository;
import com.project.soc.repository.PhishingScanRepository;
import com.project.soc.repository.SecurityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final IncidentReportRepository incidentReportRepository;
    private final AlertRepository alertRepository;
    private final SecurityLogRepository securityLogRepository;
    private final PhishingScanRepository phishingScanRepository;
    private final AiContentGenerator aiContentGenerator;
    private final DomainMapper domainMapper;

    @Value("${app.ai.gemini-api-key:}")
    private String geminiApiKey;

    @Transactional
    public IncidentReportResponse generateDaily() {
        long logCount = securityLogRepository.count();
        long alertCount = alertRepository.count();
        long phishCount = phishingScanRepository.count();
        String ctx = "logs_total=" + logCount + ", alerts_total=" + alertCount + ", phishing_scans_total=" + phishCount;
        String content = aiContentGenerator.generateDailySummary(ctx);
        GeneratedBy by = (geminiApiKey != null && !geminiApiKey.isBlank()) ? GeneratedBy.AI : GeneratedBy.SYSTEM;
        IncidentReport r = IncidentReport.builder()
                .title("Daily threat report " + LocalDate.now(ZoneOffset.UTC))
                .reportType(ReportType.DAILY_SUMMARY)
                .content(content)
                .generatedBy(by)
                .build();
        r = incidentReportRepository.save(r);
        return domainMapper.toIncidentReportResponse(r);
    }

    @Transactional
    public IncidentReportResponse generateForAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));
        Optional<SecurityLog> log = Optional.empty();
        if (alert.getSourceLogId() != null) {
            log = securityLogRepository.findById(alert.getSourceLogId());
        }
        String content = aiContentGenerator.generateIncidentReport(alert, log, Optional.empty());
        GeneratedBy by = (geminiApiKey != null && !geminiApiKey.isBlank()) ? GeneratedBy.AI : GeneratedBy.SYSTEM;
        IncidentReport r = IncidentReport.builder()
                .title("Incident report: " + alert.getTitle())
                .reportType(ReportType.INCIDENT_DETAIL)
                .relatedAlertId(alert.getId())
                .content(content)
                .generatedBy(by)
                .build();
        r = incidentReportRepository.save(r);
        return domainMapper.toIncidentReportResponse(r);
    }

    @Transactional
    public IncidentReportResponse generateForPhishing(Long scanId) {
        PhishingScan scan = phishingScanRepository.findById(scanId)
                .orElseThrow(() -> new ResourceNotFoundException("Phishing scan not found"));
        String content = aiContentGenerator.generatePhishingExplanation(scan);
        GeneratedBy by = (geminiApiKey != null && !geminiApiKey.isBlank()) ? GeneratedBy.AI : GeneratedBy.SYSTEM;
        IncidentReport r = IncidentReport.builder()
                .title("Phishing analysis report")
                .reportType(ReportType.PHISHING_REPORT)
                .content(content)
                .generatedBy(by)
                .build();
        r = incidentReportRepository.save(r);
        return domainMapper.toIncidentReportResponse(r);
    }

    @Transactional(readOnly = true)
    public Page<IncidentReportResponse> list(Pageable pageable) {
        return incidentReportRepository.findAll(pageable).map(domainMapper::toIncidentReportResponse);
    }

    @Transactional(readOnly = true)
    public IncidentReportResponse getById(Long id) {
        IncidentReport r = incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        return domainMapper.toIncidentReportResponse(r);
    }

    @Transactional(readOnly = true)
    public String downloadText(Long id) {
        return incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"))
                .getContent();
    }
}

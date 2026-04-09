package com.project.soc.service;

import com.project.soc.dto.admin.AdminStatsResponse;
import com.project.soc.repository.AlertRepository;
import com.project.soc.repository.IncidentReportRepository;
import com.project.soc.repository.PhishingScanRepository;
import com.project.soc.repository.SecurityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final SecurityLogRepository securityLogRepository;
    private final AlertRepository alertRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final PhishingScanRepository phishingScanRepository;

    @Transactional(readOnly = true)
    public AdminStatsResponse stats() {
        return AdminStatsResponse.builder()
                .totalLogs(securityLogRepository.count())
                .totalAlerts(alertRepository.count())
                .totalReports(incidentReportRepository.count())
                .totalPhishingScans(phishingScanRepository.count())
                .build();
    }
}

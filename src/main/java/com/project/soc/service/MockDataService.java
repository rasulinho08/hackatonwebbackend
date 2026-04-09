package com.project.soc.service;

import com.project.soc.dto.log.LogResponse;
import com.project.soc.dto.phishing.PhishingAnalyzeResponse;
import com.project.soc.entity.SecurityLog;
import com.project.soc.enums.EventType;
import com.project.soc.enums.LogStatus;
import com.project.soc.enums.Severity;
import com.project.soc.enums.SourceType;
import com.project.soc.mapper.DomainMapper;
import com.project.soc.repository.SecurityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MockDataService {

    private final SecurityLogRepository securityLogRepository;
    private final AlertService alertService;
    private final DomainMapper domainMapper;
    private final RiskService riskService;

    @Transactional
    public List<LogResponse> generateMockLogs(int count) {
        List<LogResponse> out = new ArrayList<>();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < count; i++) {
            Severity sev = pickSeverity(rnd);
            EventType et = EventType.values()[rnd.nextInt(EventType.values().length)];
            SourceType st = SourceType.MOCK;
            String ip = "10.0." + rnd.nextInt(256) + "." + rnd.nextInt(256);
            SecurityLog log = SecurityLog.builder()
                    .sourceType(st)
                    .eventType(et)
                    .rawMessage("Simulated SOC event #" + rnd.nextInt(1_000_000) + " — " + et.name())
                    .ipAddress(ip)
                    .hostname("host-" + rnd.nextInt(500))
                    .username(rnd.nextBoolean() ? "user" + rnd.nextInt(200) : null)
                    .severity(sev)
                    .status(LogStatus.NEW)
                    .occurredAt(Instant.now().minusSeconds(rnd.nextInt(86_400)))
                    .riskScore(Math.min(100, sev.ordinal() * 20 + rnd.nextInt(30)))
                    .metadataJson("{\"demo\":true}")
                    .build();
            log = securityLogRepository.save(log);
            alertService.evaluateAutomationsForLog(log);
            out.add(domainMapper.toLogResponse(log));
        }
        riskService.recalculateAndPersist();
        return out;
    }

    private static Severity pickSeverity(ThreadLocalRandom rnd) {
        int r = rnd.nextInt(100);
        if (r < 10) {
            return Severity.CRITICAL;
        }
        if (r < 30) {
            return Severity.HIGH;
        }
        if (r < 60) {
            return Severity.MEDIUM;
        }
        return Severity.LOW;
    }
}

package com.project.soc.scheduler;

import com.project.soc.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true")
public class RiskSnapshotScheduler {

    private final RiskService riskService;

    @Scheduled(fixedRateString = "${app.scheduler.snapshot-interval-ms:3600000}")
    public void captureRiskSnapshot() {
        riskService.recalculateAndPersist();
    }
}

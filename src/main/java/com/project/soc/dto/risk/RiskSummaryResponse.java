package com.project.soc.dto.risk;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RiskSummaryResponse {

    private int overallRiskScore;
    private int activeThreatCount;
    private int phishingDetectedCount;
    private int criticalAlertCount;
    private int safeSystemsPercent;
    private List<String> explanations;
}

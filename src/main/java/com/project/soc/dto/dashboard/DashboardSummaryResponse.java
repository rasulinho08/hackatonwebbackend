package com.project.soc.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardSummaryResponse {

    private long totalAlerts;
    private long activeThreats;
    private int riskScore;
    private int safeSystemsPercent;
    private long phishingDetectedCount;
    private List<RecentAlertSummaryDto> recentAlerts;
    private List<RiskTrendPointDto> riskTrend;
}

package com.project.soc.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {

    private long totalAlerts;
    private long activeThreats;
    private int riskScore;
    private int safeSystemsPercent;
    private long blockedAttacks;
    private String avgResponseTime;
    private String uptime;
    private long endpoints;
}

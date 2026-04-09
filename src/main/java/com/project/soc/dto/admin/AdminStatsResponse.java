package com.project.soc.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {

    private long totalLogs;
    private long totalAlerts;
    private long totalReports;
    private long totalPhishingScans;
}

package com.project.soc.dto.dashboard;

import com.project.soc.enums.AlertStatus;
import com.project.soc.enums.Severity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RecentAlertSummaryDto {

    private Long id;
    private String title;
    private Severity severity;
    private AlertStatus status;
    private Instant createdAt;
}

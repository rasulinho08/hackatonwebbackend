package com.project.soc.dto.alert;

import com.project.soc.enums.AlertStatus;
import com.project.soc.enums.Severity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AlertResponse {

    private Long id;
    private String title;
    private String description;
    private String alertType;
    private Severity severity;
    private Long sourceLogId;
    private AlertStatus status;
    private Long assignedToUserId;
    private String aiSummary;
    private Instant createdAt;
    private Instant updatedAt;
}

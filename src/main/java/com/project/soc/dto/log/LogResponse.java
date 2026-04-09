package com.project.soc.dto.log;

import com.project.soc.enums.EventType;
import com.project.soc.enums.LogStatus;
import com.project.soc.enums.Severity;
import com.project.soc.enums.SourceType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class LogResponse {

    private Long id;
    private SourceType sourceType;
    private EventType eventType;
    private String rawMessage;
    private String ipAddress;
    private String hostname;
    private String username;
    private Severity severity;
    private LogStatus status;
    private Instant occurredAt;
    private Integer riskScore;
    private String metadataJson;
    private Instant createdAt;
}

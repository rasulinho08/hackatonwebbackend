package com.project.soc.dto.log;

import com.project.soc.enums.EventType;
import com.project.soc.enums.LogStatus;
import com.project.soc.enums.Severity;
import com.project.soc.enums.SourceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateLogRequest {

    @NotNull
    private SourceType sourceType;

    @NotNull
    private EventType eventType;

    @NotBlank
    private String rawMessage;

    private String ipAddress;
    private String hostname;
    private String username;

    @NotNull
    private Severity severity;

    private LogStatus status;

    private Instant occurredAt;

    @Min(0)
    @Max(100)
    private Integer riskScore;

    private String metadataJson;
}

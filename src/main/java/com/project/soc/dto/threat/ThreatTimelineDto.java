package com.project.soc.dto.threat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatTimelineDto {

    private Long id;
    private String eventType;
    private String description;
    private String severity;
    private Instant timestamp;
}

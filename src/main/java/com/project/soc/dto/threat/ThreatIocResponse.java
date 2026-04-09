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
public class ThreatIocResponse {

    private Long id;
    private String indicatorType;
    private String value;
    private String source;
    private String severity;
    private String description;
    private Integer hitCount;
    private Instant createdAt;
}

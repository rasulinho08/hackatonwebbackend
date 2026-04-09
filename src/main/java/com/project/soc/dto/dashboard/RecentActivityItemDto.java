package com.project.soc.dto.dashboard;

import com.project.soc.enums.Severity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RecentActivityItemDto {

    private String type;
    private Long id;
    private String title;
    private Severity severity;
    private Instant occurredAt;
}

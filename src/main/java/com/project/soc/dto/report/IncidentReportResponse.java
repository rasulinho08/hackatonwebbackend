package com.project.soc.dto.report;

import com.project.soc.enums.GeneratedBy;
import com.project.soc.enums.ReportType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class IncidentReportResponse {

    private Long id;
    private String title;
    private ReportType reportType;
    private Long relatedAlertId;
    private String content;
    private GeneratedBy generatedBy;
    private Instant createdAt;
}

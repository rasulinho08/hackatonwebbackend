package com.project.soc.dto.vuln;

import com.project.soc.enums.ScanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VulnScanResponse {

    private Long id;
    private String target;
    private ScanStatus status;
    private Integer totalVulnerabilities;
    private Integer criticalCount;
    private Integer highCount;
    private Integer mediumCount;
    private Integer lowCount;
    private Instant startedAt;
    private Instant completedAt;
}

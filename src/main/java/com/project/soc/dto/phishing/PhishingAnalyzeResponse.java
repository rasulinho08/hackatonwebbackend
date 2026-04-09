package com.project.soc.dto.phishing;

import com.project.soc.enums.PhishingLabel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PhishingAnalyzeResponse {

    private Long scanId;
    private PhishingLabel predictedLabel;
    private BigDecimal confidenceScore;
    private String explanation;
    private List<String> indicators;
}

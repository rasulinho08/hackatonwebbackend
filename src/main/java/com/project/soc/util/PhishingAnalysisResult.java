package com.project.soc.util;

import com.project.soc.enums.PhishingLabel;

import java.math.BigDecimal;
import java.util.List;

public record PhishingAnalysisResult(
        PhishingLabel label,
        BigDecimal confidenceScore,
        String explanation,
        List<String> indicators
) {
}

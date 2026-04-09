package com.project.soc.dto.phishing;

import com.project.soc.enums.PhishingLabel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PhishingScanResponse {

    private Long id;
    private String emailSubject;
    private String senderEmail;
    private String emailBody;
    private PhishingLabel predictedLabel;
    private BigDecimal confidenceScore;
    private String explanation;
    private String extractedIndicatorsJson;
    private Instant createdAt;
}

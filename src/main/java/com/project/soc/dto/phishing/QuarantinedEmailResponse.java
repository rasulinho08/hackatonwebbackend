package com.project.soc.dto.phishing;

import com.project.soc.enums.QuarantineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuarantinedEmailResponse {

    private Long id;
    private String senderEmail;
    private String subject;
    private String body;
    private Long phishingScanId;
    private String verdict;
    private Double confidenceScore;
    private QuarantineStatus status;
    private Instant createdAt;
}

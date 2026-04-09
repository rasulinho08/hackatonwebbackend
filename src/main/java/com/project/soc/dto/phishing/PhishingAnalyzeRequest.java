package com.project.soc.dto.phishing;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhishingAnalyzeRequest {

    @NotBlank
    @Email
    private String senderEmail;

    @NotBlank
    private String subject;

    @NotBlank
    private String body;
}

package com.project.soc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.soc.config.AppProperties;
import com.project.soc.dto.phishing.PhishingAnalyzeRequest;
import com.project.soc.dto.phishing.PhishingAnalyzeResponse;
import com.project.soc.dto.phishing.PhishingScanResponse;
import com.project.soc.entity.PhishingScan;
import com.project.soc.enums.PhishingLabel;
import com.project.soc.exception.ResourceNotFoundException;
import com.project.soc.mapper.DomainMapper;
import com.project.soc.repository.PhishingScanRepository;
import com.project.soc.util.PhishingAnalysisResult;
import com.project.soc.util.PhishingRuleEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhishingService {

    private final PhishingScanRepository phishingScanRepository;
    private final AlertService alertService;
    private final RiskService riskService;
    private final ObjectMapper objectMapper;
    private final DomainMapper domainMapper;
    private final AppProperties appProperties;

    @Transactional
    public PhishingAnalyzeResponse analyze(PhishingAnalyzeRequest req) {
        PhishingAnalysisResult r = PhishingRuleEngine.analyze(req.getSenderEmail(), req.getSubject(), req.getBody());
        String indicatorsJson = toJson(r.indicators());
        PhishingScan scan = PhishingScan.builder()
                .emailSubject(req.getSubject())
                .senderEmail(req.getSenderEmail())
                .emailBody(req.getBody())
                .predictedLabel(r.label())
                .confidenceScore(r.confidenceScore())
                .explanation(r.explanation())
                .extractedIndicatorsJson(indicatorsJson)
                .build();
        scan = phishingScanRepository.save(scan);

        boolean shouldAlert = r.label() == PhishingLabel.PHISHING
                || (r.label() == PhishingLabel.SUSPICIOUS
                && r.confidenceScore().doubleValue() >= appProperties.getRisk().getPhishingAlertConfidenceThreshold());
        if (shouldAlert) {
            alertService.createFromPhishingScan(scan);
        }
        riskService.recalculateAndPersist();

        return PhishingAnalyzeResponse.builder()
                .scanId(scan.getId())
                .predictedLabel(r.label())
                .confidenceScore(r.confidenceScore())
                .explanation(r.explanation())
                .indicators(r.indicators())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PhishingScanResponse> history(Pageable pageable) {
        return phishingScanRepository.findAll(pageable).map(domainMapper::toPhishingScanResponse);
    }

    @Transactional(readOnly = true)
    public PhishingScanResponse getById(Long id) {
        PhishingScan s = phishingScanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phishing scan not found"));
        return domainMapper.toPhishingScanResponse(s);
    }

    @Transactional
    public PhishingAnalyzeResponse simulate() {
        PhishingAnalyzeRequest req = new PhishingAnalyzeRequest();
        req.setSenderEmail("security-alert@paypa1-verify.net");
        req.setSubject("URGENT: Verify your account now or it will be suspended");
        req.setBody("""
                Dear user, your account has unusual activity. Please verify now at
                http://malicious-example.test/login and enter your password to restore access.
                This is time sensitive. Attachment: invoice.zip
                """);
        return analyze(req);
    }

    private String toJson(java.util.List<String> indicators) {
        try {
            return objectMapper.writeValueAsString(indicators);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}

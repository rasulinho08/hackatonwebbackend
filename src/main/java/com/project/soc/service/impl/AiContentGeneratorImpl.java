package com.project.soc.service.impl;

import com.project.soc.entity.Alert;
import com.project.soc.entity.PhishingScan;
import com.project.soc.entity.SecurityLog;
import com.project.soc.integration.GeminiClient;
import com.project.soc.service.AiContentGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiContentGeneratorImpl implements AiContentGenerator {

    private static final String MODEL = "gemini-1.5-flash";

    private final GeminiClient geminiClient;

    @Value("${app.ai.gemini-api-key:}")
    private String apiKey;

    @Override
    public String generateIncidentReport(Alert alert, Optional<SecurityLog> relatedLog, Optional<PhishingScan> relatedScan) {
        String prompt = """
                You are a SOC analyst. Produce a concise incident report for executives.
                Include: what happened, severity, likely cause, recommended actions, affected indicators.
                Use neutral professional language. Under 400 words.
                Alert title: %s
                Alert description: %s
                Severity: %s
                Related log excerpt: %s
                Related phishing context: %s
                """.formatted(
                alert.getTitle(),
                Optional.ofNullable(alert.getDescription()).orElse(""),
                alert.getSeverity(),
                relatedLog.map(SecurityLog::getRawMessage).orElse("n/a"),
                relatedScan.map(PhishingScan::getExplanation).orElse("n/a")
        );
        return geminiClient.generateText(MODEL, prompt).orElse(mockIncident(alert, relatedLog, relatedScan));
    }

    @Override
    public String generateDailySummary(String contextBlock) {
        String prompt = """
                Summarize the following SOC telemetry for a daily threat briefing.
                Highlight trends, open critical items, and recommended next steps. Under 350 words.

                Data:
                %s
                """.formatted(contextBlock);
        return geminiClient.generateText(MODEL, prompt).orElse(mockDaily(contextBlock));
    }

    @Override
    public String generatePhishingExplanation(PhishingScan scan) {
        String prompt = """
                Explain this phishing assessment for a security analyst. Under 200 words.
                Subject: %s
                Sender: %s
                Label: %s
                Confidence: %s
                Indicators JSON: %s
                """.formatted(
                scan.getEmailSubject(),
                scan.getSenderEmail(),
                scan.getPredictedLabel(),
                scan.getConfidenceScore(),
                Optional.ofNullable(scan.getExtractedIndicatorsJson()).orElse("[]")
        );
        return geminiClient.generateText(MODEL, prompt).orElse(mockPhishing(scan));
    }

    private static String mockIncident(Alert alert, Optional<SecurityLog> relatedLog, Optional<PhishingScan> relatedScan) {
        return """
                Incident summary (mock / offline mode)
                --------------------------------------
                Title: %s
                Severity: %s

                What happened: The system recorded an event matching this alert. Review correlated logs and user-reported symptoms.

                Likely cause: Automated rules classified activity as suspicious based on telemetry and heuristics.

                Recommended actions:
                1) Validate alert fidelity and enrich with network and identity context.
                2) Contain affected endpoints or accounts if compromise is confirmed.
                3) Escalate to incident response if data exfiltration or lateral movement is observed.

                Affected indicators:
                - Related log present: %s
                - Related phishing context present: %s
                """.formatted(
                alert.getTitle(),
                alert.getSeverity(),
                relatedLog.isPresent(),
                relatedScan.isPresent()
        );
    }

    private static String mockDaily(String contextBlock) {
        return """
                Daily threat summary (mock / offline mode)
                -------------------------------------------
                The following telemetry snapshot was provided for summarization:

                %s

                Key takeaways: prioritize open high/critical alerts, validate phishing findings with mailbox controls,
                and ensure failed authentication spikes are correlated to the same source IPs.
                """.formatted(contextBlock.length() > 2000 ? contextBlock.substring(0, 2000) + "..." : contextBlock);
    }

    private static String mockPhishing(PhishingScan scan) {
        return """
                Phishing assessment (mock / offline mode)
                -----------------------------------------
                Classification: %s with confidence %s.
                Analyst notes: %s
                Next steps: quarantine message, reset creds if user engaged, and update mail gateway rules.
                """.formatted(scan.getPredictedLabel(), scan.getConfidenceScore(), scan.getExplanation());
    }
}

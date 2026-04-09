package com.project.soc.util;

import com.project.soc.enums.PhishingLabel;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Rule-based phishing scoring. Replace with ML or external classifier when ready.
 */
@UtilityClass
public class PhishingRuleEngine {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s]+", Pattern.CASE_INSENSITIVE);
    private static final List<String> URGENT = List.of(
            "urgent", "verify now", "verify immediately", "account suspended", "suspended",
            "password reset", "reset your password", "confirm your identity", "act now",
            "wire transfer", "unusual activity", "locked", "immediately"
    );
    private static final List<String> CREDENTIAL = List.of(
            "enter your password", "confirm password", "ssn", "social security",
            "credit card", "cvv", "otp", "verification code"
    );
    private static final List<String> SPOOF_HINTS = List.of("paypal-security", "microsoft-update", "amazon-support");

    public PhishingAnalysisResult analyze(String senderEmail, String subject, String body) {
        String combined = (subject + " " + body).toLowerCase(Locale.ROOT);
        List<String> indicators = new ArrayList<>();
        int score = 0;

        for (String u : URGENT) {
            if (combined.contains(u)) {
                indicators.add("urgent_language");
                score += 12;
                break;
            }
        }
        if (URL_PATTERN.matcher(subject + " " + body).find()) {
            indicators.add("external_link");
            score += 15;
        }
        for (String c : CREDENTIAL) {
            if (combined.contains(c)) {
                indicators.add("credential_request");
                score += 18;
                break;
            }
        }
        if (combined.contains("attachment") || combined.contains(".zip") || combined.contains(".exe")) {
            indicators.add("attachment_mention");
            score += 8;
        }
        String senderLower = senderEmail.toLowerCase(Locale.ROOT);
        for (String hint : SPOOF_HINTS) {
            if (senderLower.contains(hint)) {
                indicators.add("spoofed_domain_pattern");
                score += 20;
                break;
            }
        }
        if (senderLower.matches(".*\\d{4,}.*@.*")) {
            indicators.add("numeric_sender_local_part");
            score += 6;
        }
        if (combined.length() > 20 && combined.chars().filter(ch -> ch == '!').count() > 3) {
            indicators.add("excessive_punctuation");
            score += 5;
        }

        PhishingLabel label;
        if (score >= 45) {
            label = PhishingLabel.PHISHING;
        } else if (score >= 22) {
            label = PhishingLabel.SUSPICIOUS;
        } else {
            label = PhishingLabel.SAFE;
        }

        BigDecimal confidence = BigDecimal.valueOf(Math.min(99, score + 15))
                .setScale(1, RoundingMode.HALF_UP);

        String explanation = buildExplanation(label, indicators);

        return new PhishingAnalysisResult(label, confidence, explanation, List.copyOf(indicators));
    }

    private static String buildExplanation(PhishingLabel label, List<String> indicators) {
        if (label == PhishingLabel.SAFE) {
            return "No strong phishing indicators detected using heuristic rules.";
        }
        if (indicators.contains("credential_request") && indicators.contains("external_link")) {
            return "Email contains credential-related language and an external link.";
        }
        if (indicators.contains("urgent_language")) {
            return "Email uses urgent or pressure language often seen in phishing.";
        }
        if (indicators.contains("spoofed_domain_pattern")) {
            return "Sender pattern resembles common brand-spoofing attempts.";
        }
        return "Several heuristic indicators suggest phishing or suspicious content.";
    }
}

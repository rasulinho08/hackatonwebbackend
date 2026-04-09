package com.project.soc.service;

import com.project.soc.entity.Alert;
import com.project.soc.entity.PhishingScan;
import com.project.soc.entity.SecurityLog;

import java.util.Optional;

/**
 * Abstraction for AI-generated narrative content. Plug in Gemini/OpenAI or ML services later.
 */
public interface AiContentGenerator {

    String generateIncidentReport(Alert alert, Optional<SecurityLog> relatedLog, Optional<PhishingScan> relatedScan);

    String generateDailySummary(String contextBlock);

    String generatePhishingExplanation(PhishingScan scan);
}

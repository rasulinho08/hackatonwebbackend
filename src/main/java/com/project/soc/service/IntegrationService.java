package com.project.soc.service;

import com.project.soc.dto.integration.IntegrationStatusResponse;
import com.project.soc.entity.IntegrationConfig;
import com.project.soc.exception.IntegrationException;
import com.project.soc.integration.GeminiClient;
import com.project.soc.repository.IntegrationConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * External integration façade. TODO: Wazuh webhook ingestion, OpenSearch, Kafka consumers, Slack/Telegram.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationService {

    private final IntegrationConfigRepository integrationConfigRepository;
    private final WebClient.Builder webClientBuilder;
    private final GeminiClient geminiClient;

    @Value("${app.ai.gemini-api-key:}")
    private String geminiApiKey;

    @Transactional(readOnly = true)
    public String testWazuh() {
        // TODO: call Wazuh indexer/cluster health or OpenSearch _cluster/health when wired.
        IntegrationConfig cfg = integrationConfigRepository.findByProviderNameIgnoreCase("WAZUH")
                .orElse(null);
        if (cfg == null || !Boolean.TRUE.equals(cfg.getEnabled()) || cfg.getBaseUrl() == null || cfg.getBaseUrl().isBlank()) {
            return "not_configured";
        }
        try {
            WebClient client = webClientBuilder.baseUrl(cfg.getBaseUrl()).build();
            client.get().uri("/").retrieve().toBodilessEntity().block();
            return "reachable_placeholder";
        } catch (Exception e) {
            log.debug("Wazuh test failed: {}", e.getMessage());
            return "unreachable";
        }
    }

    @Transactional(readOnly = true)
    public String testGemini() {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return "not_configured";
        }
        return geminiClient.generateText("gemini-1.5-flash", "Reply with OK only.").map(r -> "ok").orElse("error");
    }

    @Transactional(readOnly = true)
    public IntegrationStatusResponse status() {
        Map<String, String> providers = new LinkedHashMap<>();
        for (IntegrationConfig c : integrationConfigRepository.findAll()) {
            String state = Boolean.TRUE.equals(c.getEnabled()) ? "enabled" : "disabled";
            if (c.getLastSyncAt() != null) {
                state = state + ", last_sync=" + c.getLastSyncAt();
            }
            providers.put(c.getProviderName(), state);
        }
        if (!providers.containsKey("WAZUH")) {
            providers.put("WAZUH", "not_configured");
        }
        if (!providers.containsKey("GEMINI")) {
            providers.put("GEMINI", geminiApiKey == null || geminiApiKey.isBlank() ? "not_configured" : "api_key_present");
        }
        return IntegrationStatusResponse.builder().providers(providers).build();
    }

    @Transactional
    public void touchSync(String provider) {
        IntegrationConfig cfg = integrationConfigRepository.findByProviderNameIgnoreCase(provider)
                .orElseThrow(() -> new IntegrationException("Unknown provider: " + provider));
        cfg.setLastSyncAt(Instant.now());
        integrationConfigRepository.save(cfg);
    }
}

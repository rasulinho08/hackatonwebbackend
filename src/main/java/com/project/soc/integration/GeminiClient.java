package com.project.soc.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.Optional;

/**
 * Minimal Gemini REST caller. TODO: expand with structured JSON schema, retries, and quotas.
 */
@Component
@Slf4j
public class GeminiClient {

    private final WebClient webClient;
    private final String apiKey;

    public GeminiClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.ai.gemini-api-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${app.ai.gemini-api-key:}") String apiKey
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public Optional<String> generateText(String model, String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        String path = "/models/" + model + ":generateContent";
        Map<String, Object> body = Map.of(
                "contents", java.util.List.of(
                        Map.of("parts", java.util.List.of(Map.of("text", prompt)))
                )
        );
        try {
            Map<?, ?> response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("key", apiKey)
                            .build())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return Optional.ofNullable(extractText(response));
        } catch (WebClientResponseException e) {
            log.warn("Gemini call failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static String extractText(Map<?, ?> response) {
        if (response == null) {
            return null;
        }
        Object candidates = response.get("candidates");
        if (!(candidates instanceof java.util.List<?> list) || list.isEmpty()) {
            return null;
        }
        Object first = list.get(0);
        if (!(first instanceof Map<?, ?> cMap)) {
            return null;
        }
        Object content = cMap.get("content");
        if (!(content instanceof Map<?, ?> contentMap)) {
            return null;
        }
        Object parts = contentMap.get("parts");
        if (!(parts instanceof java.util.List<?> partsList) || partsList.isEmpty()) {
            return null;
        }
        Object part0 = partsList.get(0);
        if (!(part0 instanceof Map<?, ?> pMap)) {
            return null;
        }
        Object text = pMap.get("text");
        return text != null ? text.toString() : null;
    }
}

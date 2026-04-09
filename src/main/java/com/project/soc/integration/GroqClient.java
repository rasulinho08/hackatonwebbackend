package com.project.soc.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class GroqClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    public GroqClient(
            WebClient.Builder builder,
            @Value("${app.ai.groq-api-key:}") String apiKey,
            @Value("${app.ai.groq-model:llama-3.3-70b-versatile}") String model
    ) {
        this.webClient = builder.baseUrl("https://api.groq.com/openai/v1").build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Optional<String> chat(String systemPrompt, String userMessage) {
        if (!isAvailable()) {
            return Optional.empty();
        }
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.3,
                "max_tokens", 2048
        );
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return Optional.ofNullable(extractContent(resp));
        } catch (WebClientResponseException e) {
            log.warn("Groq API error: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Groq call failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static String extractContent(Map<String, Object> resp) {
        if (resp == null) return null;
        Object choices = resp.get("choices");
        if (!(choices instanceof List<?> list) || list.isEmpty()) return null;
        Object first = list.get(0);
        if (!(first instanceof Map<?, ?> choiceMap)) return null;
        Object message = choiceMap.get("message");
        if (!(message instanceof Map<?, ?> msgMap)) return null;
        Object content = msgMap.get("content");
        return content != null ? content.toString() : null;
    }
}

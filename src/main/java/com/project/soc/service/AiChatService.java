package com.project.soc.service;

import com.project.soc.dto.ai.AiChatResponse;
import com.project.soc.dto.stats.StatsResponse;
import com.project.soc.entity.Alert;
import com.project.soc.integration.GroqClient;
import com.project.soc.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final GroqClient groqClient;
    private final StatsService statsService;
    private final AlertRepository alertRepository;

    private static final String SYSTEM_PROMPT = """
            You are SOC Sentinel AI — an intelligent cybersecurity assistant integrated into a Security Operations Center.
            You have access to real-time dashboard data provided as context below.
            Answer the user's security-related questions concisely and professionally.
            If the user asks about alerts, threats, or system status, use the provided data.
            If you can't answer from the data, say so clearly.
            Format your response in markdown when helpful.
            """;

    @Transactional(readOnly = true)
    public AiChatResponse chat(String userMessage) {
        StatsResponse stats = statsService.getStats();
        List<Alert> recentAlerts = alertRepository.findAll(
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();

        String alertContext = recentAlerts.stream()
                .map(a -> String.format("[%s] %s — %s (%s)", a.getSeverity(), a.getTitle(), a.getStatus(), a.getCreatedAt()))
                .collect(Collectors.joining("\n"));

        String context = """
                === SOC Dashboard Stats ===
                Total Alerts: %d
                Active Threats: %d
                Risk Score: %d
                Safe Systems: %d%%
                Blocked Attacks: %d
                Avg Response Time: %s
                Uptime: %s
                
                === Recent Alerts (last 20) ===
                %s
                """.formatted(
                stats.getTotalAlerts(), stats.getActiveThreats(),
                stats.getRiskScore(), stats.getSafeSystemsPercent(),
                stats.getBlockedAttacks(), stats.getAvgResponseTime(),
                stats.getUptime(), alertContext
        );

        String fullPrompt = SYSTEM_PROMPT + "\n\n" + context;
        Optional<String> aiResponse = groqClient.chat(fullPrompt, userMessage);

        String response = aiResponse.orElseGet(() -> generateMockResponse(userMessage, stats, recentAlerts));

        List<Map<String, Object>> data = new ArrayList<>();
        if (userMessage.toLowerCase().contains("alert") || userMessage.toLowerCase().contains("threat")) {
            for (Alert a : recentAlerts.stream().limit(5).toList()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", a.getId());
                item.put("title", a.getTitle());
                item.put("severity", a.getSeverity().name());
                item.put("status", a.getStatus().name());
                item.put("createdAt", a.getCreatedAt().toString());
                data.add(item);
            }
        }

        return AiChatResponse.builder()
                .response(response)
                .data(data.isEmpty() ? null : data)
                .build();
    }

    private String generateMockResponse(String message, StatsResponse stats, List<Alert> alerts) {
        String lower = message.toLowerCase();
        if (lower.contains("critical") || lower.contains("high")) {
            long critical = alerts.stream().filter(a -> a.getSeverity().name().equals("CRITICAL")).count();
            long high = alerts.stream().filter(a -> a.getSeverity().name().equals("HIGH")).count();
            return "Based on current data, there are **%d critical** and **%d high** severity alerts. Risk score is **%d/100**. Immediate investigation is recommended for any unresolved critical alerts."
                    .formatted(critical, high, stats.getRiskScore());
        }
        if (lower.contains("status") || lower.contains("summary") || lower.contains("overview")) {
            return """
                    ## SOC Status Summary
                    - **Total Alerts:** %d
                    - **Active Threats:** %d
                    - **Risk Score:** %d/100
                    - **Safe Systems:** %d%%
                    - **Blocked Attacks:** %d
                    - **Uptime:** %s
                    
                    The system is actively monitoring. %s
                    """.formatted(
                    stats.getTotalAlerts(), stats.getActiveThreats(),
                    stats.getRiskScore(), stats.getSafeSystemsPercent(),
                    stats.getBlockedAttacks(), stats.getUptime(),
                    stats.getRiskScore() > 70 ? "**Warning: Risk level is elevated.**" : "Risk levels are within acceptable range."
            );
        }
        if (lower.contains("brute") || lower.contains("login")) {
            long loginAlerts = alerts.stream().filter(a -> a.getAlertType() != null && a.getAlertType().contains("LOGIN")).count();
            return "There are **%d** login-related alerts in the recent history. Monitor failed authentication patterns and consider implementing rate limiting on affected endpoints."
                    .formatted(loginAlerts);
        }
        return "I've analyzed your query against current SOC data. There are **%d total alerts** with a risk score of **%d/100**. For more specific analysis, try asking about specific threat types, severity levels, or time ranges."
                .formatted(stats.getTotalAlerts(), stats.getRiskScore());
    }
}

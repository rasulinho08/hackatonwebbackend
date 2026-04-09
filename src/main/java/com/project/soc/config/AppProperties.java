package com.project.soc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private boolean demoMode;
    private Risk risk = new Risk();
    private Scheduler scheduler = new Scheduler();

    @Data
    public static class Risk {
        private int failedLoginThreshold = 5;
        private int phishingAlertConfidenceThreshold = 60;
    }

    @Data
    public static class Scheduler {
        private boolean enabled;
        private long snapshotIntervalMs = 3_600_000L;
    }
}

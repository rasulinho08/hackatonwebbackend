package com.project.soc;

import com.project.soc.config.AppProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableScheduling
@EnableAsync
public class SocBackendApplication {

    public static void main(String[] args) {
        loadDotEnvIfPresent();
        SpringApplication.run(SocBackendApplication.class, args);
    }

    /**
     * Reads {@code .env} from the working directory when present.
     * Does not override variables already set by the OS / hosting (Railway, Render, etc.).
     */
    private static void loadDotEnvIfPresent() {
        Dotenv dotenv = Dotenv.configure().directory("./").ignoreIfMissing().load();
        dotenv.entries().forEach(e -> {
            String key = e.getKey();
            String val = e.getValue();
            if (val == null || val.isBlank()) {
                return;
            }
            if (System.getenv(key) != null) {
                return;
            }
            if (System.getProperty(key) != null) {
                return;
            }
            System.setProperty(key, val);
        });
    }
}

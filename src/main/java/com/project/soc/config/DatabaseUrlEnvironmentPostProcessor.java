package com.project.soc.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Render/Heroku-style {@code DATABASE_URL} ({@code postgres://} / {@code postgresql://}) is not valid
 * for Spring JDBC. Maps it to {@code spring.datasource.*} when {@code DB_URL} is not set.
 * <p>
 * Runs for any non-{@code test} profile: on Render, {@code getActiveProfiles()} is often still empty
 * when this runs, so a {@code prod}-only check skipped mapping and left the datasource on {@code localhost}.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE = "mappedDatabaseUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isTestProfileActive(environment)) {
            return;
        }

        String dbUrl = environment.getProperty("DB_URL");
        if (dbUrl != null && !dbUrl.isBlank()) {
            return;
        }

        String databaseUrl = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                System.getenv("DATABASE_URL")
        );
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        Map<String, String> mapped = mapDatabaseUrl(databaseUrl);
        if (mapped.isEmpty()) {
            // Visible in Render logs — otherwise failure looks like a random Hibernate error.
            System.err.println("[soc-backend] DATABASE_URL is set but JDBC mapping failed. "
                    + "Use pooler URL from Supabase (IPv4), URL-encode special chars in password, "
                    + "or set DB_URL (jdbc:postgresql://...) + DB_USERNAME + DB_PASSWORD instead.");
            return;
        }

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("spring.datasource.url", mapped.get("url"));
        props.put("spring.datasource.username", mapped.get("username"));
        props.put("spring.datasource.password", mapped.get("password"));

        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE, new HashMap<>(props)));
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    private static boolean isTestProfileActive(ConfigurableEnvironment environment) {
        for (String p : environment.getActiveProfiles()) {
            if ("test".equalsIgnoreCase(p)) {
                return true;
            }
        }
        String explicit = environment.getProperty("spring.profiles.active");
        if (explicit != null && explicit.toLowerCase().contains("test")) {
            return true;
        }
        String envProfiles = System.getenv("SPRING_PROFILES_ACTIVE");
        return envProfiles != null && envProfiles.toLowerCase().contains("test");
    }

    /**
     * Parses postgres / postgresql URI into JDBC URL + user + password.
     */
    static Map<String, String> mapDatabaseUrl(String databaseUrl) {
        Map<String, String> out = new HashMap<>();
        try {
            String normalized = databaseUrl.trim();
            if (normalized.startsWith("postgres://")) {
                normalized = "postgresql://" + normalized.substring("postgres://".length());
            }
            URI uri = URI.create(normalized);
            String userInfo = uri.getRawUserInfo();
            if (userInfo == null || userInfo.isBlank()) {
                return out;
            }
            int colon = userInfo.indexOf(':');
            String user = colon >= 0 ? urlDecode(userInfo.substring(0, colon)) : urlDecode(userInfo);
            String password = colon >= 0 && colon < userInfo.length() - 1
                    ? urlDecode(userInfo.substring(colon + 1))
                    : "";

            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return out;
            }
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                return out;
            }
            String database = path.startsWith("/") ? path.substring(1) : path;
            int q = database.indexOf('?');
            if (q >= 0) {
                database = database.substring(0, q);
            }

            String jdbc = "jdbc:postgresql://" + host + ":" + port + "/" + database;
            String query = uri.getQuery();
            if (query != null && !query.isBlank()) {
                jdbc = jdbc + "?" + query;
            } else {
                jdbc = jdbc + "?sslmode=require";
            }

            out.put("url", jdbc);
            out.put("username", user);
            out.put("password", password);
        } catch (Exception e) {
            System.err.println("[soc-backend] DATABASE_URL parse error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return out;
    }

    private static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}

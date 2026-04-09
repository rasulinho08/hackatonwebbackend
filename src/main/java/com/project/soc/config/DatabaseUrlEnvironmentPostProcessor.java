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
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE = "mappedDatabaseUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isProdProfileActive(environment)) {
            return;
        }

        String dbUrl = environment.getProperty("DB_URL");
        if (dbUrl != null && !dbUrl.isBlank()) {
            return;
        }

        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        Map<String, String> mapped = mapDatabaseUrl(databaseUrl);
        if (mapped.isEmpty()) {
            return;
        }

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("spring.datasource.url", mapped.get("url"));
        props.put("spring.datasource.username", mapped.get("username"));
        props.put("spring.datasource.password", mapped.get("password"));

        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE, new HashMap<>(props)));
    }

    private static boolean isProdProfileActive(ConfigurableEnvironment environment) {
        for (String p : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(p)) {
                return true;
            }
        }
        String explicit = environment.getProperty("spring.profiles.active");
        if (explicit != null) {
            for (String p : explicit.split(",")) {
                if ("prod".equalsIgnoreCase(p.trim())) {
                    return true;
                }
            }
        }
        return false;
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
        } catch (Exception ignored) {
            // leave empty; Spring will surface a clearer datasource error
        }
        return out;
    }

    private static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}

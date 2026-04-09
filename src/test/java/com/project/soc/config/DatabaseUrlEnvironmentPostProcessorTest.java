package com.project.soc.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void mapsPostgresqlUrl() {
        Map<String, String> m = DatabaseUrlEnvironmentPostProcessor.mapDatabaseUrl(
                "postgresql://myuser:mypass@dpg-test.oregon-postgres.render.com:5432/mydb");
        assertThat(m.get("url")).isEqualTo("jdbc:postgresql://dpg-test.oregon-postgres.render.com:5432/mydb?sslmode=require");
        assertThat(m.get("username")).isEqualTo("myuser");
        assertThat(m.get("password")).isEqualTo("mypass");
    }

    @Test
    void mapsPostgresSchemeAndDefaultPort() {
        Map<String, String> m = DatabaseUrlEnvironmentPostProcessor.mapDatabaseUrl(
                "postgres://u:p@db.internal/dbname");
        assertThat(m.get("url")).startsWith("jdbc:postgresql://db.internal:5432/dbname");
        assertThat(m.get("username")).isEqualTo("u");
        assertThat(m.get("password")).isEqualTo("p");
    }
}

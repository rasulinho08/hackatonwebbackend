package com.project.soc.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "integration_configs", indexes = {
        @Index(name = "uk_integration_provider", columnList = "provider_name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String providerName;

    @Column(columnDefinition = "TEXT")
    private String baseUrl;

    @Column(length = 64)
    private String apiKeyMasked;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = Boolean.FALSE;

    private Instant lastSyncAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}

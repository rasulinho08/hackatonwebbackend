package com.project.soc.entity;

import com.project.soc.enums.EventType;
import com.project.soc.enums.LogStatus;
import com.project.soc.enums.Severity;
import com.project.soc.enums.SourceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "security_logs", indexes = {
        @Index(columnList = "severity"),
        @Index(columnList = "source_type"),
        @Index(columnList = "event_type"),
        @Index(columnList = "status"),
        @Index(columnList = "occurred_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 48)
    private EventType eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawMessage;

    @Column(length = 64)
    private String ipAddress;

    @Column(length = 255)
    private String hostname;

    @Column(length = 255)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    @Builder.Default
    private LogStatus status = LogStatus.NEW;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column
    private Integer riskScore;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}

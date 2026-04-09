package com.project.soc.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "threat_indicators", indexes = @Index(columnList = "indicator_type"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ThreatIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32, name = "indicator_type")
    private String indicatorType;

    @Column(nullable = false)
    private String value;

    @Column(length = 64)
    private String source;

    @Column(length = 16)
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 64)
    private String country;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    private Integer hitCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}

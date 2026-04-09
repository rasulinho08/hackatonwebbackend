package com.project.soc.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "risk_snapshots", indexes = @Index(columnList = "snapshot_time"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer overallRiskScore;

    @Column(nullable = false)
    private Integer activeThreatCount;

    @Column(nullable = false)
    private Integer phishingDetectedCount;

    @Column(nullable = false)
    private Integer criticalAlertCount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal safeSystemsPercent;

    @Column(nullable = false)
    private Instant snapshotTime;

    @Column(columnDefinition = "TEXT")
    private String explanationJson;
}

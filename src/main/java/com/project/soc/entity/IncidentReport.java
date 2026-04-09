package com.project.soc.entity;

import com.project.soc.enums.GeneratedBy;
import com.project.soc.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "incident_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReportType reportType;

    @Column(name = "related_alert_id")
    private Long relatedAlertId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GeneratedBy generatedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}

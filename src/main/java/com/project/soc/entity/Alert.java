package com.project.soc.entity;

import com.project.soc.enums.AlertStatus;
import com.project.soc.enums.Severity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "alerts", indexes = {
        @Index(columnList = "severity"),
        @Index(columnList = "status"),
        @Index(columnList = "assigned_to_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 64)
    private String alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Severity severity;

    @Column(name = "source_log_id")
    private Long sourceLogId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    @Builder.Default
    private AlertStatus status = AlertStatus.OPEN;

    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}

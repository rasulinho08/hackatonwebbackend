package com.project.soc.entity;

import com.project.soc.enums.QuarantineStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "quarantined_emails")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuarantinedEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320)
    private String senderEmail;

    @Column(nullable = false, length = 512)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    private Long phishingScanId;

    @Column(nullable = false, length = 16)
    private String verdict;

    private Double confidenceScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    @Builder.Default
    private QuarantineStatus status = QuarantineStatus.QUARANTINED;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}

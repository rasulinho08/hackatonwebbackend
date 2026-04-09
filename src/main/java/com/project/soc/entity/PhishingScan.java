package com.project.soc.entity;

import com.project.soc.enums.PhishingLabel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "phishing_scans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhishingScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String emailSubject;

    @Column(nullable = false, length = 320)
    private String senderEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String emailBody;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PhishingLabel predictedLabel;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String extractedIndicatorsJson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}

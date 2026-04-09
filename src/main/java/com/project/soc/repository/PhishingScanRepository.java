package com.project.soc.repository;

import com.project.soc.entity.PhishingScan;
import com.project.soc.enums.PhishingLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;

public interface PhishingScanRepository extends JpaRepository<PhishingScan, Long> {

    long countByPredictedLabelAndCreatedAtAfter(PhishingLabel label, Instant after);

    long countByPredictedLabelInAndCreatedAtAfter(Collection<PhishingLabel> labels, Instant after);
}

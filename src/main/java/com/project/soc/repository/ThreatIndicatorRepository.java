package com.project.soc.repository;

import com.project.soc.entity.ThreatIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ThreatIndicatorRepository extends JpaRepository<ThreatIndicator, Long> {

    @Query("SELECT t.value, SUM(t.hitCount) as hits FROM ThreatIndicator t WHERE t.indicatorType = 'IP' GROUP BY t.value ORDER BY hits DESC")
    List<Object[]> findTopAttackerIps();

    @Query("SELECT t.indicatorType, COUNT(t) FROM ThreatIndicator t GROUP BY t.indicatorType")
    List<Object[]> countByType();
}

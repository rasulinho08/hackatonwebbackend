package com.project.soc.repository;

import com.project.soc.entity.SecurityLog;
import com.project.soc.enums.EventType;
import com.project.soc.enums.LogStatus;
import com.project.soc.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Collection;

public interface SecurityLogRepository extends JpaRepository<SecurityLog, Long>, JpaSpecificationExecutor<SecurityLog> {

    long countByEventTypeAndIpAddressAndOccurredAtAfter(EventType eventType, String ipAddress, Instant after);

    long countBySeverityAndStatusNotIn(Severity severity, Collection<LogStatus> statuses);
}

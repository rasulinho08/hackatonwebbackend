package com.project.soc.repository;

import com.project.soc.entity.Alert;
import com.project.soc.enums.AlertStatus;
import com.project.soc.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;

public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {

    long countByStatus(AlertStatus status);

    long countByStatusIn(Collection<AlertStatus> statuses);

    long countBySeverityAndStatusIn(Severity severity, Collection<AlertStatus> statuses);

    long countBySeverityInAndStatusIn(Collection<Severity> severities, Collection<AlertStatus> statuses);
}

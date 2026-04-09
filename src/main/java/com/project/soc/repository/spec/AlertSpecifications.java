package com.project.soc.repository.spec;

import com.project.soc.entity.Alert;
import com.project.soc.enums.AlertStatus;
import com.project.soc.enums.Severity;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class AlertSpecifications {

    public static Specification<Alert> filter(Severity severity, AlertStatus status, Long assignedToUserId) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (severity != null) {
                p.add(cb.equal(root.get("severity"), severity));
            }
            if (status != null) {
                p.add(cb.equal(root.get("status"), status));
            }
            if (assignedToUserId != null) {
                p.add(cb.equal(root.get("assignedToUserId"), assignedToUserId));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}

package com.project.soc.repository.spec;

import com.project.soc.entity.SecurityLog;
import com.project.soc.enums.EventType;
import com.project.soc.enums.LogStatus;
import com.project.soc.enums.Severity;
import com.project.soc.enums.SourceType;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class SecurityLogSpecifications {

    public static Specification<SecurityLog> filter(
            Severity severity,
            SourceType sourceType,
            EventType eventType,
            LogStatus status,
            Instant from,
            Instant to,
            String keyword
    ) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (severity != null) {
                p.add(cb.equal(root.get("severity"), severity));
            }
            if (sourceType != null) {
                p.add(cb.equal(root.get("sourceType"), sourceType));
            }
            if (eventType != null) {
                p.add(cb.equal(root.get("eventType"), eventType));
            }
            if (status != null) {
                p.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
            }
            if (to != null) {
                p.add(cb.lessThanOrEqualTo(root.get("occurredAt"), to));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(root.get("rawMessage")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("hostname"), cb.literal(""))), like),
                        cb.like(cb.lower(cb.coalesce(root.get("username"), cb.literal(""))), like)
                ));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}

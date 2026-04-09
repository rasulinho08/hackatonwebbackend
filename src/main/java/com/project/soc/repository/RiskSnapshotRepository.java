package com.project.soc.repository;

import com.project.soc.entity.RiskSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RiskSnapshotRepository extends JpaRepository<RiskSnapshot, Long> {

    Optional<RiskSnapshot> findFirstByOrderBySnapshotTimeDesc();

    List<RiskSnapshot> findBySnapshotTimeAfterOrderBySnapshotTimeAsc(Instant after);
}

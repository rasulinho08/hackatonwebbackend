package com.project.soc.repository;

import com.project.soc.entity.QuarantinedEmail;
import com.project.soc.enums.QuarantineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuarantinedEmailRepository extends JpaRepository<QuarantinedEmail, Long> {
    List<QuarantinedEmail> findByStatusOrderByCreatedAtDesc(QuarantineStatus status);
}

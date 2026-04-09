package com.project.soc.service;

import com.project.soc.dto.phishing.QuarantinedEmailResponse;
import com.project.soc.entity.QuarantinedEmail;
import com.project.soc.enums.QuarantineStatus;
import com.project.soc.exception.ResourceNotFoundException;
import com.project.soc.repository.QuarantinedEmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuarantineService {

    private final QuarantinedEmailRepository repo;

    @Transactional(readOnly = true)
    public List<QuarantinedEmailResponse> getQuarantined() {
        return repo.findByStatusOrderByCreatedAtDesc(QuarantineStatus.QUARANTINED)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public QuarantinedEmailResponse release(Long id) {
        QuarantinedEmail e = findOrThrow(id);
        e.setStatus(QuarantineStatus.RELEASED);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        QuarantinedEmail e = findOrThrow(id);
        e.setStatus(QuarantineStatus.DELETED);
        repo.save(e);
    }

    @Transactional
    public QuarantinedEmailResponse quarantine(String sender, String subject, String body,
                                                Long scanId, String verdict, Double confidence) {
        QuarantinedEmail q = QuarantinedEmail.builder()
                .senderEmail(sender)
                .subject(subject)
                .body(body)
                .phishingScanId(scanId)
                .verdict(verdict)
                .confidenceScore(confidence)
                .status(QuarantineStatus.QUARANTINED)
                .build();
        return toResponse(repo.save(q));
    }

    private QuarantinedEmail findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quarantined email not found"));
    }

    private QuarantinedEmailResponse toResponse(QuarantinedEmail e) {
        return QuarantinedEmailResponse.builder()
                .id(e.getId())
                .senderEmail(e.getSenderEmail())
                .subject(e.getSubject())
                .body(e.getBody())
                .phishingScanId(e.getPhishingScanId())
                .verdict(e.getVerdict())
                .confidenceScore(e.getConfidenceScore())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }
}

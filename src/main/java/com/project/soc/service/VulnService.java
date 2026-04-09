package com.project.soc.service;

import com.project.soc.dto.vuln.StartScanRequest;
import com.project.soc.dto.vuln.VulnScanResponse;
import com.project.soc.dto.vuln.VulnerabilityResponse;
import com.project.soc.entity.Vulnerability;
import com.project.soc.entity.VulnerabilityScan;
import com.project.soc.enums.ScanStatus;
import com.project.soc.exception.ResourceNotFoundException;
import com.project.soc.repository.VulnerabilityRepository;
import com.project.soc.repository.VulnerabilityScanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VulnService {

    private final VulnerabilityScanRepository scanRepo;
    private final VulnerabilityRepository vulnRepo;
    private final VulnScanAsyncProcessor vulnScanAsyncProcessor;

    @Transactional
    public VulnScanResponse startScan(StartScanRequest req) {
        VulnerabilityScan scan = VulnerabilityScan.builder()
                .target(req.getTarget())
                .status(ScanStatus.RUNNING)
                .build();
        scan = scanRepo.save(scan);
        vulnScanAsyncProcessor.completeScan(scan.getId());
        return toScanResponse(scan);
    }

    @Transactional(readOnly = true)
    public VulnScanResponse getScan(Long scanId) {
        VulnerabilityScan scan = scanRepo.findById(scanId)
                .orElseThrow(() -> new ResourceNotFoundException("Scan not found"));
        VulnScanResponse resp = toScanResponse(scan);
        return resp;
    }

    @Transactional(readOnly = true)
    public List<VulnScanResponse> getAllScans() {
        return scanRepo.findAll().stream().map(this::toScanResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<VulnerabilityResponse> getVulnerabilities() {
        return vulnRepo.findAll().stream().map(this::toVulnResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<VulnerabilityResponse> getVulnerabilitiesByScan(Long scanId) {
        return vulnRepo.findByScanId(scanId).stream().map(this::toVulnResponse).toList();
    }

    private VulnScanResponse toScanResponse(VulnerabilityScan s) {
        return VulnScanResponse.builder()
                .id(s.getId())
                .target(s.getTarget())
                .status(s.getStatus())
                .totalVulnerabilities(s.getTotalVulnerabilities())
                .criticalCount(s.getCriticalCount())
                .highCount(s.getHighCount())
                .mediumCount(s.getMediumCount())
                .lowCount(s.getLowCount())
                .startedAt(s.getStartedAt())
                .completedAt(s.getCompletedAt())
                .build();
    }

    private VulnerabilityResponse toVulnResponse(Vulnerability v) {
        return VulnerabilityResponse.builder()
                .id(v.getId())
                .cveId(v.getCveId())
                .title(v.getTitle())
                .description(v.getDescription())
                .severity(v.getSeverity())
                .cvssScore(v.getCvssScore())
                .affectedAsset(v.getAffectedAsset())
                .status(v.getStatus())
                .scanId(v.getScanId())
                .createdAt(v.getCreatedAt())
                .build();
    }
}

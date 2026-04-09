package com.project.soc.service;

import com.project.soc.entity.Vulnerability;
import com.project.soc.entity.VulnerabilityScan;
import com.project.soc.enums.ScanStatus;
import com.project.soc.enums.VulnSeverity;
import com.project.soc.repository.VulnerabilityRepository;
import com.project.soc.repository.VulnerabilityScanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Random;

/**
 * Separate bean so {@link org.springframework.scheduling.annotation.Async} is applied via Spring proxy
 * (self-invocation on {@link VulnService} would skip async).
 */
@Component
@RequiredArgsConstructor
public class VulnScanAsyncProcessor {

    private final VulnerabilityScanRepository scanRepo;
    private final VulnerabilityRepository vulnRepo;

    private static final String[][] MOCK_CVES = {
            {"CVE-2024-21762", "FortiOS Out-of-Bound Write", "CRITICAL", "9.8", "Firewall"},
            {"CVE-2024-3400", "PAN-OS Command Injection", "CRITICAL", "10.0", "VPN Gateway"},
            {"CVE-2024-1709", "ScreenConnect Auth Bypass", "HIGH", "9.8", "Remote Access"},
            {"CVE-2023-46805", "Ivanti Connect Secure Auth Bypass", "HIGH", "8.2", "VPN"},
            {"CVE-2024-0204", "GoAnywhere MFT Auth Bypass", "CRITICAL", "9.8", "File Transfer"},
            {"CVE-2024-27198", "TeamCity Auth Bypass", "HIGH", "9.8", "CI/CD Server"},
            {"CVE-2023-34362", "MOVEit Transfer SQL Injection", "CRITICAL", "9.8", "File Transfer"},
            {"CVE-2024-20353", "Cisco ASA DoS Vulnerability", "MEDIUM", "8.6", "Firewall"},
            {"CVE-2024-23897", "Jenkins Arbitrary File Read", "HIGH", "7.5", "CI/CD Server"},
            {"CVE-2024-1086", "Linux Kernel nf_tables Use-After-Free", "HIGH", "7.8", "Linux Server"},
    };

    @Async
    @Transactional
    public void completeScan(Long scanId) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        VulnerabilityScan scan = scanRepo.findById(scanId).orElse(null);
        if (scan == null) {
            return;
        }

        Random rng = new Random();
        int count = 3 + rng.nextInt(6);
        int critical = 0, high = 0, medium = 0, low = 0;

        for (int i = 0; i < count; i++) {
            String[] template = MOCK_CVES[rng.nextInt(MOCK_CVES.length)];
            VulnSeverity sev = VulnSeverity.valueOf(template[2]);
            switch (sev) {
                case CRITICAL -> critical++;
                case HIGH -> high++;
                case MEDIUM -> medium++;
                default -> low++;
            }
            Vulnerability v = Vulnerability.builder()
                    .cveId(template[0])
                    .title(template[1])
                    .description("Detected in target: " + scan.getTarget())
                    .severity(sev)
                    .cvssScore(Double.parseDouble(template[3]))
                    .affectedAsset(template[4])
                    .status("Open")
                    .scanId(scanId)
                    .build();
            vulnRepo.save(v);
        }
        scan.setStatus(ScanStatus.COMPLETED);
        scan.setTotalVulnerabilities(count);
        scan.setCriticalCount(critical);
        scan.setHighCount(high);
        scan.setMediumCount(medium);
        scan.setLowCount(low);
        scan.setCompletedAt(Instant.now());
        scanRepo.save(scan);
    }
}

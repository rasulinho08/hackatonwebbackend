package com.project.soc.config;

import com.project.soc.entity.*;
import com.project.soc.enums.*;
import com.project.soc.repository.*;
import com.project.soc.service.MockDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@Order(20)
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DemoDataInitializer implements ApplicationRunner {

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IntegrationConfigRepository integrationConfigRepository;
    private final SecurityLogRepository securityLogRepository;
    private final MockDataService mockDataService;
    private final ThreatIndicatorRepository threatIndicatorRepository;
    private final QuarantinedEmailRepository quarantinedEmailRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final VulnerabilityScanRepository vulnerabilityScanRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!appProperties.isDemoMode()) {
            return;
        }
        seedIntegrations();
        seedDemoUser();
        if (securityLogRepository.count() == 0) {
            mockDataService.generateMockLogs(12);
            log.info("Demo mode: seeded mock security logs.");
        }
        seedThreatIndicators();
        seedQuarantinedEmails();
        seedVulnerabilities();
    }

    private void seedIntegrations() {
        upsertIntegration("WAZUH", "https://wazuh.example.local", "****", false);
        upsertIntegration("GEMINI", "https://generativelanguage.googleapis.com", "****", false);
        upsertIntegration("GROQ", "https://api.groq.com", "****", false);
    }

    private void upsertIntegration(String name, String baseUrl, String masked, boolean enabled) {
        integrationConfigRepository.findByProviderNameIgnoreCase(name).orElseGet(() -> {
            IntegrationConfig c = IntegrationConfig.builder()
                    .providerName(name)
                    .baseUrl(baseUrl)
                    .apiKeyMasked(masked)
                    .enabled(enabled)
                    .build();
            return integrationConfigRepository.save(c);
        });
    }

    private void seedDemoUser() {
        String email = "admin@demo.local";
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        User u = User.builder()
                .fullName("Demo Admin")
                .email(email)
                .passwordHash(passwordEncoder.encode("DemoPass123!"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(u);
        log.info("Demo mode: created user {} / DemoPass123!", email);
    }

    private void seedThreatIndicators() {
        if (threatIndicatorRepository.count() > 0) return;

        List<ThreatIndicator> indicators = List.of(
                ThreatIndicator.builder().indicatorType("IP").value("185.220.101.34").source("AbuseIPDB")
                        .severity("CRITICAL").description("Known Tor exit node used in brute force attacks")
                        .country("Germany").latitude(51.1657).longitude(10.4515).hitCount(847).build(),
                ThreatIndicator.builder().indicatorType("IP").value("45.155.205.233").source("OTX AlienVault")
                        .severity("HIGH").description("C2 server for Cobalt Strike beacon")
                        .country("Russia").latitude(55.7558).longitude(37.6173).hitCount(523).build(),
                ThreatIndicator.builder().indicatorType("IP").value("103.253.41.98").source("VirusTotal")
                        .severity("HIGH").description("APT28 associated scanning infrastructure")
                        .country("China").latitude(35.8617).longitude(104.1954).hitCount(312).build(),
                ThreatIndicator.builder().indicatorType("DOMAIN").value("malware-c2.evil.net").source("MalwareBazaar")
                        .severity("CRITICAL").description("Active malware distribution domain")
                        .country("Netherlands").latitude(52.3676).longitude(4.9041).hitCount(198).build(),
                ThreatIndicator.builder().indicatorType("HASH").value("a1b2c3d4e5f6789012345678abcdef90").source("VirusTotal")
                        .severity("HIGH").description("SHA256 hash of Emotet payload variant")
                        .country("USA").latitude(37.0902).longitude(-95.7129).hitCount(156).build(),
                ThreatIndicator.builder().indicatorType("IP").value("91.215.85.142").source("Shodan")
                        .severity("MEDIUM").description("Scanning for exposed RDP services")
                        .country("Ukraine").latitude(48.3794).longitude(31.1656).hitCount(89).build(),
                ThreatIndicator.builder().indicatorType("URL").value("https://phish-login.example.com/verify").source("PhishTank")
                        .severity("HIGH").description("Credential harvesting page mimicking Office 365")
                        .country("Brazil").latitude(-14.235).longitude(-51.9253).hitCount(234).build(),
                ThreatIndicator.builder().indicatorType("IP").value("5.188.206.14").source("AbuseIPDB")
                        .severity("CRITICAL").description("DDoS botnet command server")
                        .country("Russia").latitude(55.7558).longitude(37.6173).hitCount(671).build(),
                ThreatIndicator.builder().indicatorType("DOMAIN").value("update-service-ms.com").source("URLhaus")
                        .severity("HIGH").description("Fake Windows Update malware dropper")
                        .country("Romania").latitude(45.9432).longitude(24.9668).hitCount(142).build(),
                ThreatIndicator.builder().indicatorType("IP").value("218.92.0.190").source("CrowdStrike")
                        .severity("CRITICAL").description("APT41 infrastructure for supply chain attacks")
                        .country("China").latitude(35.8617).longitude(104.1954).hitCount(405).build()
        );
        threatIndicatorRepository.saveAll(indicators);
        log.info("Demo mode: seeded {} threat indicators.", indicators.size());
    }

    private void seedQuarantinedEmails() {
        if (quarantinedEmailRepository.count() > 0) return;

        List<QuarantinedEmail> emails = List.of(
                QuarantinedEmail.builder()
                        .senderEmail("security@paypa1-verify.net")
                        .subject("URGENT: Verify your PayPal account immediately")
                        .body("Dear user, your account has been compromised. Click here to verify: http://paypa1-verify.net/login")
                        .verdict("PHISHING").confidenceScore(0.96).status(QuarantineStatus.QUARANTINED).build(),
                QuarantinedEmail.builder()
                        .senderEmail("admin@microsoft-365-update.com")
                        .subject("Action Required: Update your Microsoft 365 credentials")
                        .body("Your Microsoft 365 subscription will expire. Update credentials at http://ms365-update.com/auth")
                        .verdict("PHISHING").confidenceScore(0.92).status(QuarantineStatus.QUARANTINED).build(),
                QuarantinedEmail.builder()
                        .senderEmail("hr@company-benefits.org")
                        .subject("Annual Benefits Enrollment - Action Needed")
                        .body("Please review your benefits package. Download the attached form: benefits_update.exe")
                        .verdict("SUSPICIOUS").confidenceScore(0.78).status(QuarantineStatus.QUARANTINED).build(),
                QuarantinedEmail.builder()
                        .senderEmail("ceo@company-domain.co")
                        .subject("Wire Transfer Request - Confidential")
                        .body("I need you to process an urgent wire transfer of $45,000. This is confidential. Reply ASAP.")
                        .verdict("PHISHING").confidenceScore(0.89).status(QuarantineStatus.QUARANTINED).build()
        );
        quarantinedEmailRepository.saveAll(emails);
        log.info("Demo mode: seeded {} quarantined emails.", emails.size());
    }

    private void seedVulnerabilities() {
        if (vulnerabilityScanRepository.count() > 0) return;

        VulnerabilityScan scan = VulnerabilityScan.builder()
                .target("192.168.1.0/24")
                .status(ScanStatus.COMPLETED)
                .totalVulnerabilities(5)
                .criticalCount(2)
                .highCount(2)
                .mediumCount(1)
                .lowCount(0)
                .completedAt(Instant.now())
                .build();
        scan = vulnerabilityScanRepository.save(scan);

        List<Vulnerability> vulns = List.of(
                Vulnerability.builder().cveId("CVE-2024-21762").title("FortiOS Out-of-Bound Write")
                        .description("A critical vulnerability in FortiOS SSL-VPN allowing remote code execution")
                        .severity(VulnSeverity.CRITICAL).cvssScore(9.8).affectedAsset("Firewall FW-01")
                        .status("Open").scanId(scan.getId()).build(),
                Vulnerability.builder().cveId("CVE-2024-3400").title("PAN-OS Command Injection")
                        .description("Command injection in GlobalProtect gateway enabling unauthenticated RCE")
                        .severity(VulnSeverity.CRITICAL).cvssScore(10.0).affectedAsset("VPN Gateway")
                        .status("Open").scanId(scan.getId()).build(),
                Vulnerability.builder().cveId("CVE-2024-1709").title("ScreenConnect Authentication Bypass")
                        .description("Critical auth bypass allowing full administrative access")
                        .severity(VulnSeverity.HIGH).cvssScore(9.8).affectedAsset("Remote Access Server")
                        .status("Investigating").scanId(scan.getId()).build(),
                Vulnerability.builder().cveId("CVE-2024-23897").title("Jenkins Arbitrary File Read")
                        .description("Allows reading arbitrary files through CLI argument parsing")
                        .severity(VulnSeverity.HIGH).cvssScore(7.5).affectedAsset("CI/CD Server")
                        .status("Open").scanId(scan.getId()).build(),
                Vulnerability.builder().cveId("CVE-2024-20353").title("Cisco ASA DoS Vulnerability")
                        .description("Denial of service in web services of Cisco ASA")
                        .severity(VulnSeverity.MEDIUM).cvssScore(8.6).affectedAsset("Firewall ASA-02")
                        .status("Patched").scanId(scan.getId()).build()
        );
        vulnerabilityRepository.saveAll(vulns);
        log.info("Demo mode: seeded vulnerability scan with {} CVEs.", vulns.size());
    }
}

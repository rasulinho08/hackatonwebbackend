package com.project.soc.service;

import com.project.soc.dto.threat.*;
import com.project.soc.entity.Alert;
import com.project.soc.entity.ThreatIndicator;
import com.project.soc.repository.AlertRepository;
import com.project.soc.repository.ThreatIndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThreatService {

    private final ThreatIndicatorRepository threatRepo;
    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public List<ThreatIocResponse> getIocs() {
        return threatRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(t -> ThreatIocResponse.builder()
                        .id(t.getId())
                        .indicatorType(t.getIndicatorType())
                        .value(t.getValue())
                        .source(t.getSource())
                        .severity(t.getSeverity())
                        .description(t.getDescription())
                        .hitCount(t.getHitCount())
                        .createdAt(t.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopAttackerDto> getTopAttackers() {
        List<Object[]> rows = threatRepo.findTopAttackerIps();
        List<TopAttackerDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            String ip = (String) row[0];
            long hits = ((Number) row[1]).longValue();
            String country = threatRepo.findAll().stream()
                    .filter(t -> ip.equals(t.getValue()))
                    .map(ThreatIndicator::getCountry)
                    .filter(Objects::nonNull)
                    .findFirst().orElse("Unknown");
            result.add(TopAttackerDto.builder().ip(ip).attackCount(hits).country(country).build());
        }
        return result.stream().limit(10).toList();
    }

    @Transactional(readOnly = true)
    public List<ThreatDistributionDto> getDistribution() {
        List<Object[]> rows = threatRepo.countByType();
        return rows.stream()
                .map(r -> ThreatDistributionDto.builder()
                        .type((String) r[0])
                        .count(((Number) r[1]).longValue())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThreatTimelineDto> getTimeline() {
        List<Alert> recent = alertRepository.findAll(
                PageRequest.of(0, 30, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
        return recent.stream()
                .map(a -> ThreatTimelineDto.builder()
                        .id(a.getId())
                        .eventType(a.getAlertType())
                        .description(a.getTitle())
                        .severity(a.getSeverity().name())
                        .timestamp(a.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThreatGeoDto> getGeoMap() {
        Map<String, List<ThreatIndicator>> byCountry = threatRepo.findAll().stream()
                .filter(t -> t.getCountry() != null)
                .collect(Collectors.groupingBy(ThreatIndicator::getCountry));
        return byCountry.entrySet().stream()
                .map(e -> {
                    List<ThreatIndicator> items = e.getValue();
                    ThreatIndicator sample = items.get(0);
                    String maxSev = items.stream()
                            .map(ThreatIndicator::getSeverity)
                            .filter(Objects::nonNull)
                            .max(Comparator.naturalOrder())
                            .orElse("MEDIUM");
                    return ThreatGeoDto.builder()
                            .country(e.getKey())
                            .latitude(sample.getLatitude() != null ? sample.getLatitude() : 0)
                            .longitude(sample.getLongitude() != null ? sample.getLongitude() : 0)
                            .threatCount(items.size())
                            .severity(maxSev)
                            .build();
                })
                .toList();
    }
}

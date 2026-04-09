package com.project.soc.dto.threat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatGeoDto {

    private String country;
    private double latitude;
    private double longitude;
    private long threatCount;
    private String severity;
}

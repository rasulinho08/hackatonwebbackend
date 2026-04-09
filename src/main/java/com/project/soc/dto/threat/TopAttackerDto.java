package com.project.soc.dto.threat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopAttackerDto {

    private String ip;
    private long attackCount;
    private String country;
}

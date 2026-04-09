package com.project.soc.dto.network;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthDto {

    private double cpuPercent;
    private double ramPercent;
    private double diskPercent;
    private int activeSessions;
    private String status;
}

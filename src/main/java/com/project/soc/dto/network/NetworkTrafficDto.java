package com.project.soc.dto.network;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkTrafficDto {

    private String time;
    private double inboundMbps;
    private double outboundMbps;
}

package com.project.soc.dto.vuln;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartScanRequest {

    @NotBlank
    private String target;
}

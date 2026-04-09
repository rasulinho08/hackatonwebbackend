package com.project.soc.dto.integration;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class IntegrationStatusResponse {

    private Map<String, String> providers;
}

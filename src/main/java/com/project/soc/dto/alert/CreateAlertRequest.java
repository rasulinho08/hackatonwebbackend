package com.project.soc.dto.alert;

import com.project.soc.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAlertRequest {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String alertType;

    @NotNull
    private Severity severity;

    private Long sourceLogId;
}

package com.project.soc.dto.alert;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignAlertRequest {

    @NotNull
    private Long userId;
}

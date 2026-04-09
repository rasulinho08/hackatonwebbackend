package com.project.soc.dto.alert;

import com.project.soc.enums.AlertStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAlertStatusRequest {

    @NotNull
    private AlertStatus status;
}

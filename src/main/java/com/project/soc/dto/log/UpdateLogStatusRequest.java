package com.project.soc.dto.log;

import com.project.soc.enums.LogStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLogStatusRequest {

    @NotNull
    private LogStatus status;
}

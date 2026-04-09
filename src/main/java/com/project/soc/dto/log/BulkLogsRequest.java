package com.project.soc.dto.log;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkLogsRequest {

    @NotEmpty
    @Valid
    private List<CreateLogRequest> logs;
}

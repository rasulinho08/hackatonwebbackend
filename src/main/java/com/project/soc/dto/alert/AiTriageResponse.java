package com.project.soc.dto.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTriageResponse {

    private Long alertId;
    private String priority;
    private int priorityScore;
    private String explanation;
    private List<String> suggestedActions;
    private String groupTag;
}

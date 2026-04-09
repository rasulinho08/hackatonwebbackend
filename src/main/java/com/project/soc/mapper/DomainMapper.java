package com.project.soc.mapper;

import com.project.soc.dto.alert.AlertResponse;
import com.project.soc.dto.auth.UserResponse;
import com.project.soc.dto.log.LogResponse;
import com.project.soc.dto.phishing.PhishingScanResponse;
import com.project.soc.dto.report.IncidentReportResponse;
import com.project.soc.entity.Alert;
import com.project.soc.entity.IncidentReport;
import com.project.soc.entity.PhishingScan;
import com.project.soc.entity.SecurityLog;
import com.project.soc.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DomainMapper {

    UserResponse toUserResponse(User user);

    LogResponse toLogResponse(SecurityLog log);

    AlertResponse toAlertResponse(Alert alert);

    PhishingScanResponse toPhishingScanResponse(PhishingScan scan);

    IncidentReportResponse toIncidentReportResponse(IncidentReport report);
}

package com.vishakha.position_doctor_project.domain.alert.dto;

import com.vishakha.position_doctor_project.common.dto.AlertSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Health signal and position risk alert DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionAlertDto {

    private String alertId;
    private String positionId;
    private String portfolioId;
    private String title;
    private String message;
    private AlertSeverity severity;
    private boolean acknowledged;
    private LocalDateTime createdAt;
}

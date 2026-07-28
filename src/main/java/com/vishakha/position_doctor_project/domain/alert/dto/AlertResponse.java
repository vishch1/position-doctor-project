package com.vishakha.position_doctor_project.domain.alert.dto;

import com.vishakha.position_doctor_project.common.dto.AlertSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Alert notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private UUID id;
    private UUID userId;
    private UUID portfolioId;
    private UUID positionId;
    private String title;
    private String message;
    private AlertSeverity severity;
    private boolean read;
    private LocalDateTime createdAt;
}

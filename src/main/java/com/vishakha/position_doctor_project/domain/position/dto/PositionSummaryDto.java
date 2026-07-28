package com.vishakha.position_doctor_project.domain.position.dto;

import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Placeholder DTO for trading position details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionSummaryDto {

    private String positionId;
    private String portfolioId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal entryPrice;
    private BigDecimal currentPrice;
    private BigDecimal unrealizedPnL;
    private BigDecimal returnPercentage;
    private PositionStatus status;
    private RiskLevel riskLevel;
    private LocalDateTime openedAt;
    private LocalDateTime lastUpdatedAt;
}

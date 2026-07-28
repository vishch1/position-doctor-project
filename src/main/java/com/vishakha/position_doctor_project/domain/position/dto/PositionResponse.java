package com.vishakha.position_doctor_project.domain.position.dto;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.dto.PositionType;
import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Output Response DTO for Position details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponse {

    private UUID id;
    private UUID portfolioId;
    private String symbol;
    private Exchange exchange;
    private PositionType positionType;
    private BigDecimal quantity;
    private BigDecimal entryPrice;
    private BigDecimal currentPrice;
    private BigDecimal unrealizedPnL;
    private BigDecimal stopLossPrice;
    private BigDecimal takeProfitPrice;
    private PositionStatus status;
    private RiskLevel riskLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

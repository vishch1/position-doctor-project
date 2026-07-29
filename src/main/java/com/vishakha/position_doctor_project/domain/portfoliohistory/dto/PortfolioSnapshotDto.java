package com.vishakha.position_doctor_project.domain.portfoliohistory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a historical snapshot of a portfolio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSnapshotDto {

    private UUID id;
    private UUID portfolioId;
    private LocalDateTime snapshotTime;
    private BigDecimal portfolioValue;
    private BigDecimal totalInvestment;
    private BigDecimal unrealizedPnL;
    private int healthScore;
    private int openPositions;
    private BigDecimal dayPnL;
}

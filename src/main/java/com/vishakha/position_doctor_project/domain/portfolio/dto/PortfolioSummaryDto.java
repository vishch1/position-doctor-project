package com.vishakha.position_doctor_project.domain.portfolio.dto;

import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Portfolio summary and exposure data structure placeholder.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryDto {

    private String portfolioId;
    private String userId;
    private String name;
    private BigDecimal totalValue;
    private BigDecimal totalUnrealizedPnL;
    private BigDecimal totalExposure;
    private int openPositionCount;
    private RiskLevel aggregatedRiskLevel;
    private LocalDateTime lastEvaluatedAt;
}

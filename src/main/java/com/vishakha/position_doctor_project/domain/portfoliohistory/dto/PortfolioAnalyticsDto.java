package com.vishakha.position_doctor_project.domain.portfoliohistory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO containing calculated analytics metrics for a portfolio over a historical timeframe.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioAnalyticsDto {

    private BigDecimal highestValue;
    private BigDecimal lowestValue;
    private BigDecimal maxDrawdown;
    private double maxDrawdownPercent;
    private double averageDailyReturn;
    private BigDecimal bestDayPnL;
    private BigDecimal worstDayPnL;
}

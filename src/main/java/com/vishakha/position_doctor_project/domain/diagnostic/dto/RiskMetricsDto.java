package com.vishakha.position_doctor_project.domain.diagnostic.dto;

import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Quant risk metrics DTO for financial positions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskMetricsDto {

    private BigDecimal valueAtRiskVaR;
    private BigDecimal maxDrawdownPercent;
    private BigDecimal sharpeRatio;
    private BigDecimal beta;
    private BigDecimal volatility;
    private RiskLevel assessedRiskLevel;
}

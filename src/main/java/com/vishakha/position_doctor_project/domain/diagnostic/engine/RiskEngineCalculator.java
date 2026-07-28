package com.vishakha.position_doctor_project.domain.diagnostic.engine;

import com.vishakha.position_doctor_project.domain.diagnostic.dto.RiskMetricsDto;
import com.vishakha.position_doctor_project.domain.position.dto.PositionSummaryDto;

/**
 * Strategy interface for calculating position risk metrics.
 */
public interface RiskEngineCalculator {

    RiskMetricsDto calculateMetrics(PositionSummaryDto position);

    int calculateHealthScore(PositionSummaryDto position, RiskMetricsDto metrics);
}

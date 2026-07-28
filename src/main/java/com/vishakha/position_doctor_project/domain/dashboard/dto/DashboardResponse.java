package com.vishakha.position_doctor_project.domain.dashboard.dto;

import com.vishakha.position_doctor_project.domain.alert.dto.AlertResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.position.dto.PositionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated response DTO for Position Doctor executive dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private PortfolioSummaryDto portfolioSummary;
    private HealthSummaryDto healthSummary;
    private List<AlertResponse> recentAlerts;
    private List<RecommendationResponse> recommendations;
    private List<PositionResponse> openPositions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioSummaryDto {
        private BigDecimal totalPortfolioValue;
        private BigDecimal totalUnrealizedPnL;
        private BigDecimal todayPnL;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthSummaryDto {
        private int overallHealthScore;
        private int healthyPositions;
        private int warningPositions;
        private int criticalPositions;
    }
}

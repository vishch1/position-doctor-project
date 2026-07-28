package com.vishakha.position_doctor_project.domain.dashboard.service;

import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.domain.alert.dto.AlertResponse;
import com.vishakha.position_doctor_project.domain.alert.service.AlertService;
import com.vishakha.position_doctor_project.domain.dashboard.dto.DashboardResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.service.DiagnosisService;
import com.vishakha.position_doctor_project.domain.diagnostic.service.RecommendationService;
import com.vishakha.position_doctor_project.domain.position.dto.PositionResponse;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation aggregating backend modules into an executive dashboard summary.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final PositionRepository positionRepository;
    private final DiagnosisService diagnosisService;
    private final RecommendationService recommendationService;
    private final AlertService alertService;

    @Override
    public DashboardResponse getDashboardSummary() {
        List<Position> openPositions = positionRepository.findByStatus(PositionStatus.OPEN);

        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        BigDecimal totalUnrealizedPnL = BigDecimal.ZERO;

        List<PositionResponse> openPositionDtos = new ArrayList<>();
        List<RecommendationResponse> recommendations = new ArrayList<>();

        int totalHealthScoreSum = 0;
        int healthyCount = 0;
        int warningCount = 0;
        int criticalCount = 0;

        for (Position pos : openPositions) {
            BigDecimal price = pos.getCurrentPrice() != null ? pos.getCurrentPrice() : pos.getEntryPrice();
            BigDecimal posValue = price.multiply(pos.getQuantity());
            totalPortfolioValue = totalPortfolioValue.add(posValue);

            if (pos.getUnrealizedPnL() != null) {
                totalUnrealizedPnL = totalUnrealizedPnL.add(pos.getUnrealizedPnL());
            }

            openPositionDtos.add(mapToPositionResponse(pos));

            PositionHealthReport report = diagnosisService.getPositionHealthReport(pos.getId());
            int score = report.getHealthScore();
            totalHealthScoreSum += score;

            if (score >= 75) {
                healthyCount++;
            } else if (score >= 40) {
                warningCount++;
            } else {
                criticalCount++;
            }

            RecommendationResponse rec = recommendationService.getRecommendation(pos.getId());
            recommendations.add(rec);
        }

        int overallHealthScore = openPositions.isEmpty() ? 100 : totalHealthScoreSum / openPositions.size();

        DashboardResponse.PortfolioSummaryDto portfolioSummary = DashboardResponse.PortfolioSummaryDto.builder()
                .totalPortfolioValue(totalPortfolioValue)
                .totalUnrealizedPnL(totalUnrealizedPnL)
                .todayPnL(totalUnrealizedPnL)
                .build();

        DashboardResponse.HealthSummaryDto healthSummary = DashboardResponse.HealthSummaryDto.builder()
                .overallHealthScore(overallHealthScore)
                .healthyPositions(healthyCount)
                .warningPositions(warningCount)
                .criticalPositions(criticalCount)
                .build();

        List<AlertResponse> recentAlerts = alertService.getAllAlerts().stream()
                .limit(5)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .portfolioSummary(portfolioSummary)
                .healthSummary(healthSummary)
                .recentAlerts(recentAlerts)
                .recommendations(recommendations)
                .openPositions(openPositionDtos)
                .build();
    }

    private PositionResponse mapToPositionResponse(Position pos) {
        return PositionResponse.builder()
                .id(pos.getId())
                .portfolioId(pos.getPortfolio() != null ? pos.getPortfolio().getId() : null)
                .symbol(pos.getSymbol())
                .exchange(pos.getExchange())
                .positionType(pos.getPositionType())
                .quantity(pos.getQuantity())
                .entryPrice(pos.getEntryPrice())
                .currentPrice(pos.getCurrentPrice())
                .stopLossPrice(pos.getStopLossPrice())
                .takeProfitPrice(pos.getTakeProfitPrice())
                .unrealizedPnL(pos.getUnrealizedPnL())
                .status(pos.getStatus())
                .riskLevel(pos.getRiskLevel())
                .createdAt(pos.getCreatedAt())
                .updatedAt(pos.getUpdatedAt())
                .build();
    }
}

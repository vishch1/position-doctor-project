package com.vishakha.position_doctor_project.domain.diagnostic.engine;

import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationAction;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Diagnosis Engine orchestrator aggregating health score calculation, risk level mapping, and recommendation generation.
 */
@Component
@RequiredArgsConstructor
public class DiagnosisEngine {

    private final HealthScoreCalculator healthScoreCalculator;
    private final RecommendationEngine recommendationEngine;

    public PositionHealthReport generateReport(Position position) {
        int healthScore = healthScoreCalculator.calculateHealthScore(position);
        RiskLevel riskLevel = determineRiskLevel(healthScore);
        RecommendationResponse rec = recommendationEngine.generateRecommendation(position, healthScore);

        return PositionHealthReport.builder()
                .positionId(position.getId())
                .symbol(position.getSymbol())
                .healthScore(healthScore)
                .riskLevel(riskLevel)
                .recommendation(RecommendationAction.valueOf(rec.getRecommendation().name()))
                .reason(rec.getReason())
                .build();
    }

    private RiskLevel determineRiskLevel(int healthScore) {
        if (healthScore >= 80) {
            return RiskLevel.LOW;
        } else if (healthScore >= 60) {
            return RiskLevel.MODERATE;
        } else if (healthScore >= 40) {
            return RiskLevel.HIGH;
        } else {
            return RiskLevel.CRITICAL;
        }
    }
}

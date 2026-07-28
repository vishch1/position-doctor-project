package com.vishakha.position_doctor_project.domain.alert.engine;

import com.vishakha.position_doctor_project.common.dto.AlertSeverity;
import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import com.vishakha.position_doctor_project.domain.alert.entity.Alert;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationType;
import com.vishakha.position_doctor_project.domain.portfolio.entity.Portfolio;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertEvaluatorTest {

    private AlertEvaluator evaluator;
    private Position samplePosition;

    @BeforeEach
    void setUp() {
        evaluator = new AlertEvaluator();

        User sampleUser = User.builder().id(UUID.randomUUID()).email("trader@test.com").build();
        Portfolio samplePortfolio = Portfolio.builder().id(UUID.randomUUID()).user(sampleUser).name("Main").build();

        samplePosition = Position.builder()
                .id(UUID.randomUUID())
                .portfolio(samplePortfolio)
                .symbol("AAPL")
                .build();
    }

    @Test
    @DisplayName("evaluateAlert - Triggers Alert when recommendation changes")
    void testRecommendationChange_TriggersAlert() {
        PositionHealthReport healthReport = PositionHealthReport.builder()
                .positionId(samplePosition.getId())
                .symbol("AAPL")
                .healthScore(50)
                .riskLevel(RiskLevel.HIGH)
                .build();

        RecommendationResponse recResponse = RecommendationResponse.builder()
                .recommendation(RecommendationType.TIGHTEN_STOPLOSS)
                .confidence(80)
                .reason("Score 50/100")
                .build();

        Alert priorAlert = Alert.builder()
                .newHealthScore(70)
                .newRecommendation(RecommendationType.HOLD)
                .build();

        Optional<Alert> alertResult = evaluator.evaluateAlert(samplePosition, healthReport, recResponse, Optional.of(priorAlert));

        assertTrue(alertResult.isPresent());
        assertEquals(AlertSeverity.WARNING, alertResult.get().getSeverity());
        assertEquals(RecommendationType.TIGHTEN_STOPLOSS, alertResult.get().getNewRecommendation());
    }

    @Test
    @DisplayName("evaluateAlert - Triggers Alert when health score drops by >= 10 points")
    void testScoreShift_TriggersAlert() {
        PositionHealthReport healthReport = PositionHealthReport.builder()
                .positionId(samplePosition.getId())
                .symbol("AAPL")
                .healthScore(55)
                .riskLevel(RiskLevel.HIGH)
                .build();

        RecommendationResponse recResponse = RecommendationResponse.builder()
                .recommendation(RecommendationType.HOLD)
                .confidence(80)
                .reason("Score shifted")
                .build();

        Alert priorAlert = Alert.builder()
                .newHealthScore(70) // 70 - 55 = 15 points shift
                .newRecommendation(RecommendationType.HOLD)
                .build();

        Optional<Alert> alertResult = evaluator.evaluateAlert(samplePosition, healthReport, recResponse, Optional.of(priorAlert));

        assertTrue(alertResult.isPresent());
        assertEquals(55, alertResult.get().getNewHealthScore());
    }
}

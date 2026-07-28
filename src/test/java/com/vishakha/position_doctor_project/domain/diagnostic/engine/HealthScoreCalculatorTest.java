package com.vishakha.position_doctor_project.domain.diagnostic.engine;

import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.dto.PositionType;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthScoreCalculatorTest {

    private HealthScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HealthScoreCalculator();
    }

    @Test
    @DisplayName("calculateHealthScore - Profitable position with SL buffer scores high")
    void testCalculateHealthScore_ProfitablePosition() {
        Position position = Position.builder()
                .id(UUID.randomUUID())
                .symbol("AAPL")
                .positionType(PositionType.LONG)
                .entryPrice(BigDecimal.valueOf(100.00))
                .currentPrice(BigDecimal.valueOf(125.00)) // +25% return
                .stopLossPrice(BigDecimal.valueOf(95.00))  // >10% buffer
                .takeProfitPrice(BigDecimal.valueOf(130.00))
                .status(PositionStatus.OPEN)
                .build();

        int score = calculator.calculateHealthScore(position);

        assertTrue(score >= 80, "Expected high health score (>= 80), but got: " + score);
    }

    @Test
    @DisplayName("calculateHealthScore - Breached stop-loss scores critical low")
    void testCalculateHealthScore_StopLossBreached() {
        Position position = Position.builder()
                .id(UUID.randomUUID())
                .symbol("TSLA")
                .positionType(PositionType.LONG)
                .entryPrice(BigDecimal.valueOf(200.00))
                .currentPrice(BigDecimal.valueOf(180.00)) // -10% return
                .stopLossPrice(BigDecimal.valueOf(185.00)) // Current price <= Stop Loss
                .status(PositionStatus.OPEN)
                .build();

        int score = calculator.calculateHealthScore(position);

        assertTrue(score < 35, "Expected critical health score (< 35), but got: " + score);
    }

    @Test
    @DisplayName("calculateHealthScore - Closed position returns neutral score")
    void testCalculateHealthScore_ClosedPosition() {
        Position position = Position.builder()
                .id(UUID.randomUUID())
                .status(PositionStatus.CLOSED)
                .build();

        int score = calculator.calculateHealthScore(position);

        assertEquals(50, score);
    }
}

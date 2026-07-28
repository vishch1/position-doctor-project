package com.vishakha.position_doctor_project.domain.diagnostic.engine;

import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationType;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationEngineTest {

    private RecommendationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RecommendationEngine();
    }

    @Test
    @DisplayName("generateRecommendation - Score > 80 returns HOLD with 95 confidence")
    void testScoreAbove80_ReturnsHold() {
        Position position = Position.builder().id(UUID.randomUUID()).symbol("AAPL").unrealizedPnL(BigDecimal.valueOf(500)).build();
        RecommendationResponse response = engine.generateRecommendation(position, 85);

        assertEquals(RecommendationType.HOLD, response.getRecommendation());
        assertEquals(95, response.getConfidence());
    }

    @Test
    @DisplayName("generateRecommendation - Score 40-60 returns TIGHTEN_STOPLOSS with 80 confidence")
    void testScore40to60_ReturnsTightenStoploss() {
        Position position = Position.builder().id(UUID.randomUUID()).symbol("MSFT").unrealizedPnL(BigDecimal.valueOf(100)).build();
        RecommendationResponse response = engine.generateRecommendation(position, 50);

        assertEquals(RecommendationType.TIGHTEN_STOPLOSS, response.getRecommendation());
        assertEquals(80, response.getConfidence());
    }

    @Test
    @DisplayName("generateRecommendation - Score 25-40 with positive PnL returns BOOK_PROFIT")
    void testScore25to40_PositivePnL_ReturnsBookProfit() {
        Position position = Position.builder().id(UUID.randomUUID()).symbol("GOOGL").unrealizedPnL(BigDecimal.valueOf(150)).build();
        RecommendationResponse response = engine.generateRecommendation(position, 30);

        assertEquals(RecommendationType.BOOK_PROFIT, response.getRecommendation());
        assertEquals(85, response.getConfidence());
    }

    @Test
    @DisplayName("generateRecommendation - Score 25-40 with negative PnL returns ADD")
    void testScore25to40_NegativePnL_ReturnsAdd() {
        Position position = Position.builder().id(UUID.randomUUID()).symbol("AMZN").unrealizedPnL(BigDecimal.valueOf(-50)).build();
        RecommendationResponse response = engine.generateRecommendation(position, 30);

        assertEquals(RecommendationType.ADD, response.getRecommendation());
        assertEquals(70, response.getConfidence());
    }

    @Test
    @DisplayName("generateRecommendation - Score < 25 returns EXIT with 95 confidence")
    void testScoreBelow25_ReturnsExit() {
        Position position = Position.builder().id(UUID.randomUUID()).symbol("NFLX").unrealizedPnL(BigDecimal.valueOf(-300)).build();
        RecommendationResponse response = engine.generateRecommendation(position, 15);

        assertEquals(RecommendationType.EXIT, response.getRecommendation());
        assertEquals(95, response.getConfidence());
    }
}

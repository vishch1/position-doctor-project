package com.vishakha.position_doctor_project.domain.diagnostic;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.dto.PositionType;
import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import com.vishakha.position_doctor_project.domain.dashboard.dto.DashboardResponse;
import com.vishakha.position_doctor_project.domain.dashboard.service.DashboardService;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.engine.DiagnosisEngine;
import com.vishakha.position_doctor_project.domain.diagnostic.service.DiagnosisService;
import com.vishakha.position_doctor_project.domain.marketdata.provider.MarketDataProvider;
import com.vishakha.position_doctor_project.domain.marketdata.service.MarketDataService;
import com.vishakha.position_doctor_project.domain.portfolio.entity.Portfolio;
import com.vishakha.position_doctor_project.domain.portfolio.repository.PortfolioRepository;
import com.vishakha.position_doctor_project.domain.position.dto.PositionResponse;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.position.repository.PositionRepository;
import com.vishakha.position_doctor_project.domain.position.service.PositionService;
import com.vishakha.position_doctor_project.domain.user.entity.User;
import com.vishakha.position_doctor_project.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class RiskConsistencyIntegrationTest {

    @Autowired
    private DiagnosisEngine diagnosisEngine;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PositionRepository positionRepository;

    @MockitoBean
    private MarketDataProvider marketDataProvider;

    private User testUser;
    private Portfolio testPortfolio;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .email("test.risk.consistency@" + UUID.randomUUID() + ".com")
                .password("Password123!")
                .firstName("Risk")
                .lastName("Test")
                .build());

        testPortfolio = portfolioRepository.save(Portfolio.builder()
                .user(testUser)
                .name("Risk Consistency Portfolio")
                .currency("USD")
                .build());
    }

    @Test
    @DisplayName("Single Source of Truth Test: DiagnosisEngine, Position API, Dashboard, and Market Update must output identical RiskLevel")
    void testRiskLevelConsistencyAcrossAllModules() {
        // Create an underwater position with -50% loss (entry 200, current 100)
        Position position = Position.builder()
                .portfolio(testPortfolio)
                .symbol("CRIT")
                .exchange(Exchange.NASDAQ)
                .positionType(PositionType.LONG)
                .quantity(BigDecimal.TEN)
                .entryPrice(BigDecimal.valueOf(200.00))
                .currentPrice(BigDecimal.valueOf(100.00))
                .unrealizedPnL(BigDecimal.valueOf(-1000.00))
                .stopLossPrice(BigDecimal.valueOf(180.00))
                .takeProfitPrice(BigDecimal.valueOf(250.00))
                .status(PositionStatus.OPEN)
                .riskLevel(RiskLevel.MODERATE)
                .build();

        position = positionRepository.save(position);

        // 1. Evaluate report using DiagnosisEngine
        PositionHealthReport healthReport = diagnosisService.getPositionHealthReport(position.getId());
        RiskLevel engineRiskLevel = healthReport.getRiskLevel();

        // 2. Query via PositionService API
        PositionResponse positionResponse = positionService.getPositionById(position.getId());
        RiskLevel positionApiRiskLevel = positionResponse.getRiskLevel();

        // 3. Query via DashboardService API
        DashboardResponse dashboardResponse = dashboardService.getDashboardSummary();
        PositionResponse dashboardPositionDto = dashboardResponse.getOpenPositions().stream()
                .filter(p -> p.getId().equals(positionResponse.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(dashboardPositionDto, "Position must be present in Dashboard");
        RiskLevel dashboardRiskLevel = dashboardPositionDto.getRiskLevel();

        // Verify all 3 modules report IDENTICAL risk level (CRITICAL)
        assertEquals(engineRiskLevel, positionApiRiskLevel, "Position API risk level must match Diagnosis Engine");
        assertEquals(engineRiskLevel, dashboardRiskLevel, "Dashboard risk level must match Diagnosis Engine");
        assertEquals(RiskLevel.CRITICAL, engineRiskLevel, "Underwater position must be CRITICAL");

        // 4. Update market data to recover price (entry 200, current 210) -> Health recovers to LOW risk
        when(marketDataProvider.fetchLatestPrice(eq("CRIT"), eq(Exchange.NASDAQ), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(210.00));

        marketDataService.updateOpenPositionsMarketData();

        // Re-check after market update
        PositionResponse updatedPositionResponse = positionService.getPositionById(position.getId());
        PositionHealthReport updatedReport = diagnosisService.getPositionHealthReport(position.getId());

        assertEquals(RiskLevel.LOW, updatedReport.getRiskLevel(), "Recovered price must update to LOW risk level");
        assertEquals(RiskLevel.LOW, updatedPositionResponse.getRiskLevel(), "Position API must reflect updated LOW risk level");
    }
}

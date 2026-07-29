package com.vishakha.position_doctor_project.common;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.dto.PositionType;
import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import com.vishakha.position_doctor_project.domain.alert.engine.AlertEvaluator;
import com.vishakha.position_doctor_project.domain.alert.repository.AlertRepository;
import com.vishakha.position_doctor_project.domain.alert.scheduler.ScheduledAlertMonitor;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationAction;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationType;
import com.vishakha.position_doctor_project.domain.diagnostic.service.DiagnosisService;
import com.vishakha.position_doctor_project.domain.diagnostic.service.RecommendationService;
import com.vishakha.position_doctor_project.domain.portfolio.dto.CreatePortfolioRequest;
import com.vishakha.position_doctor_project.domain.portfolio.dto.PortfolioResponse;
import com.vishakha.position_doctor_project.domain.portfolio.entity.Portfolio;
import com.vishakha.position_doctor_project.domain.portfolio.repository.PortfolioRepository;
import com.vishakha.position_doctor_project.domain.portfolio.service.PortfolioServiceImpl;
import com.vishakha.position_doctor_project.domain.position.dto.CreatePositionRequest;
import com.vishakha.position_doctor_project.domain.position.dto.PositionResponse;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.position.repository.PositionRepository;
import com.vishakha.position_doctor_project.domain.position.service.PositionServiceImpl;
import com.vishakha.position_doctor_project.domain.user.entity.User;
import com.vishakha.position_doctor_project.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskLevelBackendTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DiagnosisService diagnosisService;

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private AlertEvaluator alertEvaluator;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private PositionServiceImpl positionService;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    @InjectMocks
    private ScheduledAlertMonitor scheduledAlertMonitor;

    @Test
    @DisplayName("RiskLevel enum must only contain LOW, MODERATE, HIGH, CRITICAL and must NOT contain UNKNOWN")
    void testRiskLevelEnumValues() {
        RiskLevel[] levels = RiskLevel.values();
        assertEquals(4, levels.length);
        assertArrayEquals(new RiskLevel[]{RiskLevel.LOW, RiskLevel.MODERATE, RiskLevel.HIGH, RiskLevel.CRITICAL}, levels);
        assertThrows(IllegalArgumentException.class, () -> RiskLevel.valueOf("UNKNOWN"));
    }

    @Test
    @DisplayName("RiskLevel.fromValue and RiskLevelConverter must map UNKNOWN string to MODERATE")
    void testUnknownValueFallbackToModerate() {
        assertEquals(RiskLevel.MODERATE, RiskLevel.fromValue("UNKNOWN"));
        assertEquals(RiskLevel.MODERATE, RiskLevel.fromValue("unknown"));
        assertEquals(RiskLevel.MODERATE, RiskLevel.fromValue("INVALID_VAL"));

        com.vishakha.position_doctor_project.common.converter.RiskLevelConverter converter =
                new com.vishakha.position_doctor_project.common.converter.RiskLevelConverter();
        assertEquals(RiskLevel.MODERATE, converter.convertToEntityAttribute("UNKNOWN"));
        assertEquals(RiskLevel.MODERATE, converter.convertToEntityAttribute(null));
        assertEquals(RiskLevel.MODERATE, converter.convertToEntityAttribute(""));
        assertEquals(RiskLevel.LOW, converter.convertToEntityAttribute("LOW"));
    }

    @Test
    @DisplayName("Position entity must default riskLevel to MODERATE")
    void testPositionEntityDefaultRiskLevel() {
        Position position = new Position();
        assertEquals(RiskLevel.MODERATE, position.getRiskLevel());
    }

    @Test
    @DisplayName("Portfolio entity must default aggregatedRiskLevel to MODERATE")
    void testPortfolioEntityDefaultRiskLevel() {
        Portfolio portfolio = new Portfolio();
        assertEquals(RiskLevel.MODERATE, portfolio.getAggregatedRiskLevel());
    }

    @Test
    @DisplayName("PositionService.createPosition must initialize riskLevel as MODERATE")
    void testCreatePositionDefaultsToModerateRiskLevel() {
        UUID portfolioId = UUID.randomUUID();
        Portfolio mockPortfolio = Portfolio.builder().id(portfolioId).build();

        CreatePositionRequest request = CreatePositionRequest.builder()
                .portfolioId(portfolioId)
                .symbol("AAPL")
                .exchange(Exchange.NASDAQ)
                .positionType(PositionType.LONG)
                .quantity(BigDecimal.TEN)
                .entryPrice(BigDecimal.valueOf(150.00))
                .build();

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(mockPortfolio));
        when(positionRepository.save(any(Position.class))).thenAnswer(invocation -> {
            Position p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PositionResponse response = positionService.createPosition(request);
        assertNotNull(response);
        assertEquals(RiskLevel.MODERATE, response.getRiskLevel());
    }

    @Test
    @DisplayName("PortfolioService.createPortfolio must initialize aggregatedRiskLevel as MODERATE")
    void testCreatePortfolioDefaultsToModerateRiskLevel() {
        UUID userId = UUID.randomUUID();
        User mockUser = User.builder().id(userId).email("user@test.com").build();

        CreatePortfolioRequest request = CreatePortfolioRequest.builder()
                .name("Growth Portfolio")
                .currency("USD")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> {
            Portfolio p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PortfolioResponse response = portfolioService.createPortfolio(request);
        assertNotNull(response);
        assertEquals(RiskLevel.MODERATE, response.getAggregatedRiskLevel());
    }

    @Test
    @DisplayName("ScheduledAlertMonitor executes monitorPositionsForAlerts without throwing exception")
    void testScheduledAlertMonitorRunsWithoutException() {
        UUID posId = UUID.randomUUID();
        Position position = Position.builder()
                .id(posId)
                .symbol("TSLA")
                .status(PositionStatus.OPEN)
                .portfolio(Portfolio.builder().id(UUID.randomUUID()).user(User.builder().id(UUID.randomUUID()).build()).build())
                .build();

        when(positionRepository.findByStatus(PositionStatus.OPEN)).thenReturn(List.of(position));

        PositionHealthReport healthReport = PositionHealthReport.builder()
                .positionId(posId)
                .symbol("TSLA")
                .healthScore(85)
                .riskLevel(RiskLevel.LOW)
                .recommendation(RecommendationAction.HOLD)
                .build();

        RecommendationResponse recResponse = RecommendationResponse.builder()
                .positionId(posId)
                .symbol("TSLA")
                .recommendation(RecommendationType.HOLD)
                .build();

        when(diagnosisService.getPositionHealthReport(posId)).thenReturn(healthReport);
        when(recommendationService.getRecommendation(posId)).thenReturn(recResponse);
        when(alertRepository.findFirstByPositionIdOrderByCreatedAtDesc(posId)).thenReturn(Optional.empty());
        when(alertEvaluator.evaluateAlert(any(), any(), any(), any())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> scheduledAlertMonitor.monitorPositionsForAlerts());

        verify(positionRepository).findByStatus(PositionStatus.OPEN);
        verify(diagnosisService).getPositionHealthReport(posId);
    }
}

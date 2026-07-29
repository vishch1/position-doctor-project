package com.vishakha.position_doctor_project.domain.portfoliohistory.service;

import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReportDto;
import com.vishakha.position_doctor_project.domain.diagnostic.service.DiagnosisService;
import com.vishakha.position_doctor_project.domain.portfolio.entity.Portfolio;
import com.vishakha.position_doctor_project.domain.portfolio.repository.PortfolioRepository;
import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioChartResponse;
import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioSnapshotDto;
import com.vishakha.position_doctor_project.domain.portfoliohistory.entity.PortfolioSnapshot;
import com.vishakha.position_doctor_project.domain.portfoliohistory.repository.PortfolioSnapshotRepository;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.position.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioHistoryServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private PortfolioSnapshotRepository snapshotRepository;

    @Mock
    private DiagnosisService diagnosisService;

    @InjectMocks
    private PortfolioHistoryServiceImpl portfolioHistoryService;

    private UUID portfolioId;
    private Portfolio samplePortfolio;

    @BeforeEach
    void setUp() {
        portfolioId = UUID.randomUUID();
        samplePortfolio = Portfolio.builder()
                .id(portfolioId)
                .name("Test Portfolio")
                .totalValue(BigDecimal.valueOf(10000.00))
                .build();
    }

    @Test
    @DisplayName("takeSnapshotsForActivePortfolios - Generates and saves snapshots for active portfolios")
    void testTakeSnapshotsForActivePortfolios() {
        Position pos = Position.builder()
                .id(UUID.randomUUID())
                .portfolio(samplePortfolio)
                .status(PositionStatus.OPEN)
                .quantity(BigDecimal.TEN)
                .entryPrice(BigDecimal.valueOf(100.00))
                .currentPrice(BigDecimal.valueOf(120.00))
                .build();

        when(portfolioRepository.findAll()).thenReturn(List.of(samplePortfolio));
        when(positionRepository.findByPortfolioId(portfolioId)).thenReturn(List.of(pos));
        when(diagnosisService.diagnosePortfolioPositions(portfolioId.toString())).thenReturn(
                List.of(PositionHealthReportDto.builder().healthScore(85).build())
        );

        portfolioHistoryService.takeSnapshotsForActivePortfolios();

        verify(snapshotRepository, times(1)).save(any(PortfolioSnapshot.class));
    }

    @Test
    @DisplayName("getHistory - Returns mapped snapshot DTOs for a valid portfolio")
    void testGetHistory_Success() {
        PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                .id(UUID.randomUUID())
                .portfolioId(portfolioId)
                .snapshotTime(LocalDateTime.now())
                .portfolioValue(BigDecimal.valueOf(12000.00))
                .totalInvestment(BigDecimal.valueOf(10000.00))
                .unrealizedPnL(BigDecimal.valueOf(2000.00))
                .healthScore(85)
                .openPositions(1)
                .dayPnL(BigDecimal.valueOf(2000.00))
                .build();

        when(portfolioRepository.existsById(portfolioId)).thenReturn(true);
        when(snapshotRepository.findByPortfolioIdOrderBySnapshotTimeAsc(portfolioId)).thenReturn(List.of(snapshot));

        List<PortfolioSnapshotDto> history = portfolioHistoryService.getHistory(portfolioId, null);

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(BigDecimal.valueOf(12000.00), history.get(0).getPortfolioValue());
    }

    @Test
    @DisplayName("getChartData - Computes highest value, lowest value, max drawdown and returns")
    void testGetChartData_AnalyticsCalculation() {
        PortfolioSnapshot s1 = PortfolioSnapshot.builder()
                .id(UUID.randomUUID())
                .portfolioId(portfolioId)
                .snapshotTime(LocalDateTime.now().minusDays(3))
                .portfolioValue(BigDecimal.valueOf(10000.00))
                .totalInvestment(BigDecimal.valueOf(10000.00))
                .unrealizedPnL(BigDecimal.ZERO)
                .dayPnL(BigDecimal.ZERO)
                .build();

        PortfolioSnapshot s2 = PortfolioSnapshot.builder()
                .id(UUID.randomUUID())
                .portfolioId(portfolioId)
                .snapshotTime(LocalDateTime.now().minusDays(2))
                .portfolioValue(BigDecimal.valueOf(15000.00)) // Peak
                .totalInvestment(BigDecimal.valueOf(10000.00))
                .unrealizedPnL(BigDecimal.valueOf(5000.00))
                .dayPnL(BigDecimal.valueOf(5000.00))
                .build();

        PortfolioSnapshot s3 = PortfolioSnapshot.builder()
                .id(UUID.randomUUID())
                .portfolioId(portfolioId)
                .snapshotTime(LocalDateTime.now().minusDays(1))
                .portfolioValue(BigDecimal.valueOf(12000.00)) // Drawdown of 3000 (20%)
                .totalInvestment(BigDecimal.valueOf(10000.00))
                .unrealizedPnL(BigDecimal.valueOf(2000.00))
                .dayPnL(BigDecimal.valueOf(-3000.00))
                .build();

        when(portfolioRepository.existsById(portfolioId)).thenReturn(true);
        when(snapshotRepository.findByPortfolioIdOrderBySnapshotTimeAsc(portfolioId)).thenReturn(List.of(s1, s2, s3));

        PortfolioChartResponse response = portfolioHistoryService.getChartData(portfolioId, null);

        assertNotNull(response);
        assertEquals(3, response.getSnapshots().size());
        assertNotNull(response.getAnalytics());
        assertEquals(BigDecimal.valueOf(15000.00), response.getAnalytics().getHighestValue());
        assertEquals(BigDecimal.valueOf(10000.00), response.getAnalytics().getLowestValue());
        assertEquals(BigDecimal.valueOf(3000.00), response.getAnalytics().getMaxDrawdown());
        assertEquals(20.0, response.getAnalytics().getMaxDrawdownPercent());
    }

    @Test
    @DisplayName("getLatestSnapshot - Throws ResourceNotFoundException when portfolio is missing")
    void testGetLatestSnapshot_NotFound() {
        when(portfolioRepository.existsById(portfolioId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> portfolioHistoryService.getLatestSnapshot(portfolioId));
    }
}

package com.vishakha.position_doctor_project.domain.portfoliohistory.controller;

import com.vishakha.position_doctor_project.common.exception.GlobalExceptionHandler;
import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioAnalyticsDto;
import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioChartResponse;
import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioSnapshotDto;
import com.vishakha.position_doctor_project.domain.portfoliohistory.service.PortfolioHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PortfolioHistoryService portfolioHistoryService;

    @InjectMocks
    private PortfolioHistoryController portfolioHistoryController;

    private UUID portfolioId;
    private PortfolioSnapshotDto sampleSnapshot;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(portfolioHistoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        portfolioId = UUID.randomUUID();
        sampleSnapshot = PortfolioSnapshotDto.builder()
                .id(UUID.randomUUID())
                .portfolioId(portfolioId)
                .snapshotTime(LocalDateTime.now())
                .portfolioValue(BigDecimal.valueOf(12500.00))
                .totalInvestment(BigDecimal.valueOf(10000.00))
                .unrealizedPnL(BigDecimal.valueOf(2500.00))
                .healthScore(90)
                .openPositions(2)
                .dayPnL(BigDecimal.valueOf(500.00))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/history/{portfolioId} - Returns HTTP 200 with list of snapshots")
    void testGetHistory_Success() throws Exception {
        when(portfolioHistoryService.getHistory(eq(portfolioId), any())).thenReturn(List.of(sampleSnapshot));

        mockMvc.perform(get("/api/v1/history/{portfolioId}", portfolioId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].portfolioValue").value(12500.00))
                .andExpect(jsonPath("$.data[0].healthScore").value(90));
    }

    @Test
    @DisplayName("GET /api/v1/history/{portfolioId}/latest - Returns HTTP 200 with latest snapshot")
    void testGetLatestSnapshot_Success() throws Exception {
        when(portfolioHistoryService.getLatestSnapshot(eq(portfolioId))).thenReturn(sampleSnapshot);

        mockMvc.perform(get("/api/v1/history/{portfolioId}/latest", portfolioId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.portfolioValue").value(12500.00));
    }

    @Test
    @DisplayName("GET /api/v1/history/{portfolioId}/chart - Returns HTTP 200 with chart data & analytics")
    void testGetChartData_Success() throws Exception {
        PortfolioAnalyticsDto analytics = PortfolioAnalyticsDto.builder()
                .highestValue(BigDecimal.valueOf(15000.00))
                .lowestValue(BigDecimal.valueOf(9000.00))
                .maxDrawdown(BigDecimal.valueOf(2000.00))
                .maxDrawdownPercent(13.33)
                .averageDailyReturn(1.25)
                .bestDayPnL(BigDecimal.valueOf(1500.00))
                .worstDayPnL(BigDecimal.valueOf(-1000.00))
                .build();

        PortfolioChartResponse chartResponse = PortfolioChartResponse.builder()
                .portfolioId(portfolioId)
                .snapshots(List.of(sampleSnapshot))
                .analytics(analytics)
                .build();

        when(portfolioHistoryService.getChartData(eq(portfolioId), any())).thenReturn(chartResponse);

        mockMvc.perform(get("/api/v1/history/{portfolioId}/chart", portfolioId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.analytics.highestValue").value(15000.00))
                .andExpect(jsonPath("$.data.analytics.maxDrawdownPercent").value(13.33));
    }
}

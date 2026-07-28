package com.vishakha.position_doctor_project.domain.dashboard.controller;

import com.vishakha.position_doctor_project.common.exception.GlobalExceptionHandler;
import com.vishakha.position_doctor_project.domain.dashboard.dto.DashboardResponse;
import com.vishakha.position_doctor_project.domain.dashboard.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private DashboardResponse sampleDashboardResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        DashboardResponse.PortfolioSummaryDto portfolioSummary = DashboardResponse.PortfolioSummaryDto.builder()
                .totalPortfolioValue(BigDecimal.valueOf(15000.00))
                .totalUnrealizedPnL(BigDecimal.valueOf(1250.00))
                .todayPnL(BigDecimal.valueOf(1250.00))
                .build();

        DashboardResponse.HealthSummaryDto healthSummary = DashboardResponse.HealthSummaryDto.builder()
                .overallHealthScore(85)
                .healthyPositions(3)
                .warningPositions(1)
                .criticalPositions(0)
                .build();

        sampleDashboardResponse = DashboardResponse.builder()
                .portfolioSummary(portfolioSummary)
                .healthSummary(healthSummary)
                .recentAlerts(Collections.emptyList())
                .recommendations(Collections.emptyList())
                .openPositions(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/dashboard - Success Scenario")
    void testGetDashboard_Success() throws Exception {
        when(dashboardService.getDashboardSummary()).thenReturn(sampleDashboardResponse);

        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.portfolioSummary.totalPortfolioValue").value(15000.00))
                .andExpect(jsonPath("$.data.healthSummary.overallHealthScore").value(85))
                .andExpect(jsonPath("$.data.healthSummary.healthyPositions").value(3));
    }
}

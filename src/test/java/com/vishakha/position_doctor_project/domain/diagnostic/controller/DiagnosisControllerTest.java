package com.vishakha.position_doctor_project.domain.diagnostic.controller;

import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import com.vishakha.position_doctor_project.common.exception.GlobalExceptionHandler;
import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationAction;
import com.vishakha.position_doctor_project.domain.diagnostic.service.DiagnosisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DiagnosisControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DiagnosisService diagnosisService;

    @InjectMocks
    private DiagnosisController diagnosisController;

    private UUID samplePositionId;
    private PositionHealthReport sampleReport;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(diagnosisController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        samplePositionId = UUID.randomUUID();

        sampleReport = PositionHealthReport.builder()
                .positionId(samplePositionId)
                .symbol("NVDA")
                .healthScore(85)
                .riskLevel(RiskLevel.LOW)
                .recommendation(RecommendationAction.TIGHTEN_STOPLOSS)
                .reason("Position shows strong return of 12.50%. Recommend adjusting stop-loss higher to lock in unrealized profits.")
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/diagnosis/{positionId} - Success Scenario")
    void testGetDiagnosis_Success() throws Exception {
        when(diagnosisService.getPositionHealthReport(eq(samplePositionId))).thenReturn(sampleReport);

        mockMvc.perform(get("/api/v1/diagnosis/{positionId}", samplePositionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.positionId").value(samplePositionId.toString()))
                .andExpect(jsonPath("$.data.symbol").value("NVDA"))
                .andExpect(jsonPath("$.data.healthScore").value(85))
                .andExpect(jsonPath("$.data.riskLevel").value("LOW"))
                .andExpect(jsonPath("$.data.recommendation").value("TIGHTEN_STOPLOSS"));
    }

    @Test
    @DisplayName("GET /api/v1/diagnosis/{positionId} - Not Found Scenario")
    void testGetDiagnosis_NotFound() throws Exception {
        when(diagnosisService.getPositionHealthReport(eq(samplePositionId)))
                .thenThrow(new ResourceNotFoundException("Position", "id", samplePositionId));

        mockMvc.perform(get("/api/v1/diagnosis/{positionId}", samplePositionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}

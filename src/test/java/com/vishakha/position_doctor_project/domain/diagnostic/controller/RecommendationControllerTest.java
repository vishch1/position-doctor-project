package com.vishakha.position_doctor_project.domain.diagnostic.controller;

import com.vishakha.position_doctor_project.common.exception.GlobalExceptionHandler;
import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationType;
import com.vishakha.position_doctor_project.domain.diagnostic.service.RecommendationService;
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
class RecommendationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    private UUID samplePositionId;
    private RecommendationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recommendationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        samplePositionId = UUID.randomUUID();

        sampleResponse = RecommendationResponse.builder()
                .positionId(samplePositionId)
                .symbol("TSLA")
                .recommendation(RecommendationType.TIGHTEN_STOPLOSS)
                .confidence(80)
                .reason("Position health score is moderate (55/100). Tightening stop-loss threshold is recommended to protect against market volatility.")
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/recommendation/{positionId} - Success Scenario")
    void testGetRecommendation_Success() throws Exception {
        when(recommendationService.getRecommendation(eq(samplePositionId))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/recommendation/{positionId}", samplePositionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.positionId").value(samplePositionId.toString()))
                .andExpect(jsonPath("$.data.symbol").value("TSLA"))
                .andExpect(jsonPath("$.data.recommendation").value("TIGHTEN_STOPLOSS"))
                .andExpect(jsonPath("$.data.confidence").value(80));
    }

    @Test
    @DisplayName("GET /api/v1/recommendation/{positionId} - Not Found Scenario")
    void testGetRecommendation_NotFound() throws Exception {
        when(recommendationService.getRecommendation(eq(samplePositionId)))
                .thenThrow(new ResourceNotFoundException("Position", "id", samplePositionId));

        mockMvc.perform(get("/api/v1/recommendation/{positionId}", samplePositionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}

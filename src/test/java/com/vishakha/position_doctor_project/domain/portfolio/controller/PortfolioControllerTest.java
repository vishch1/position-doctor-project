package com.vishakha.position_doctor_project.domain.portfolio.controller;

import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import com.vishakha.position_doctor_project.common.exception.GlobalExceptionHandler;
import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.domain.portfolio.dto.CreatePortfolioRequest;
import com.vishakha.position_doctor_project.domain.portfolio.dto.PortfolioResponse;
import com.vishakha.position_doctor_project.domain.portfolio.dto.UpdatePortfolioRequest;
import com.vishakha.position_doctor_project.domain.portfolio.service.PortfolioService;
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
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private PortfolioController portfolioController;

    private UUID sampleId;
    private UUID sampleUserId;
    private PortfolioResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(portfolioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleId = UUID.randomUUID();
        sampleUserId = UUID.randomUUID();

        sampleResponse = PortfolioResponse.builder()
                .id(sampleId)
                .userId(sampleUserId)
                .name("Growth Portfolio")
                .description("Tech & Blue Chip Stock Holdings")
                .totalValue(BigDecimal.valueOf(50000.00))
                .totalUnrealizedPnL(BigDecimal.valueOf(3500.50))
                .currency("USD")
                .aggregatedRiskLevel(RiskLevel.MODERATE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/portfolios - Success Scenario")
    void testCreatePortfolio_Success() throws Exception {
        String jsonPayload = String.format("{\"userId\":\"%s\",\"name\":\"Growth Portfolio\",\"description\":\"Tech & Blue Chip Stock Holdings\",\"currency\":\"USD\"}", sampleUserId);

        when(portfolioService.createPortfolio(any(CreatePortfolioRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Portfolio created successfully"))
                .andExpect(jsonPath("$.data.name").value("Growth Portfolio"))
                .andExpect(jsonPath("$.data.currency").value("USD"));
    }

    @Test
    @DisplayName("POST /api/v1/portfolios - Validation Failure (Missing Name and User ID)")
    void testCreatePortfolio_ValidationFailure() throws Exception {
        String invalidJsonPayload = "{\"description\":\"Missing Required Fields\"}";

        mockMvc.perform(post("/api/v1/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("GET /api/v1/portfolios/{id} - Success Scenario")
    void testGetPortfolioById_Success() throws Exception {
        when(portfolioService.getPortfolioById(eq(sampleId))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/portfolios/{id}", sampleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(sampleId.toString()))
                .andExpect(jsonPath("$.data.name").value("Growth Portfolio"));
    }

    @Test
    @DisplayName("GET /api/v1/portfolios/{id} - Resource Not Found")
    void testGetPortfolioById_NotFound() throws Exception {
        when(portfolioService.getPortfolioById(eq(sampleId)))
                .thenThrow(new ResourceNotFoundException("Portfolio", "id", sampleId));

        mockMvc.perform(get("/api/v1/portfolios/{id}", sampleId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/portfolios - Success Scenario")
    void testGetAllPortfolios_Success() throws Exception {
        when(portfolioService.getAllPortfolios(null)).thenReturn(Collections.singletonList(sampleResponse));

        mockMvc.perform(get("/api/v1/portfolios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Growth Portfolio"));
    }

    @Test
    @DisplayName("PUT /api/v1/portfolios/{id} - Success Scenario")
    void testUpdatePortfolio_Success() throws Exception {
        String updateJsonPayload = "{\"name\":\"Updated Growth Portfolio\",\"description\":\"Updated Description\",\"currency\":\"EUR\"}";

        PortfolioResponse updatedResponse = PortfolioResponse.builder()
                .id(sampleId)
                .userId(sampleUserId)
                .name("Updated Growth Portfolio")
                .description("Updated Description")
                .currency("EUR")
                .build();

        when(portfolioService.updatePortfolio(eq(sampleId), any(UpdatePortfolioRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/portfolios/{id}", sampleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Growth Portfolio"));
    }

    @Test
    @DisplayName("DELETE /api/v1/portfolios/{id} - Success Scenario")
    void testDeletePortfolio_Success() throws Exception {
        doNothing().when(portfolioService).deletePortfolio(eq(sampleId));

        mockMvc.perform(delete("/api/v1/portfolios/{id}", sampleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Portfolio deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/portfolios/{id} - Not Found Scenario")
    void testDeletePortfolio_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Portfolio", "id", sampleId))
                .when(portfolioService).deletePortfolio(eq(sampleId));

        mockMvc.perform(delete("/api/v1/portfolios/{id}", sampleId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}

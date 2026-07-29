package com.vishakha.position_doctor_project.domain.position.controller;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.dto.PositionType;
import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import com.vishakha.position_doctor_project.common.exception.GlobalExceptionHandler;
import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.domain.position.dto.CreatePositionRequest;
import com.vishakha.position_doctor_project.domain.position.dto.PositionResponse;
import com.vishakha.position_doctor_project.domain.position.dto.UpdatePositionRequest;
import com.vishakha.position_doctor_project.domain.position.service.PositionService;
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
class PositionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PositionService positionService;

    @InjectMocks
    private PositionController positionController;

    private UUID samplePositionId;
    private UUID samplePortfolioId;
    private PositionResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(positionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        samplePositionId = UUID.randomUUID();
        samplePortfolioId = UUID.randomUUID();

        sampleResponse = PositionResponse.builder()
                .id(samplePositionId)
                .portfolioId(samplePortfolioId)
                .symbol("AAPL")
                .exchange(Exchange.NASDAQ)
                .positionType(PositionType.LONG)
                .quantity(BigDecimal.valueOf(100))
                .entryPrice(BigDecimal.valueOf(150.00))
                .currentPrice(BigDecimal.valueOf(150.00))
                .unrealizedPnL(BigDecimal.ZERO)
                .status(PositionStatus.OPEN)
                .riskLevel(RiskLevel.LOW)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/positions - Success Scenario")
    void testCreatePosition_Success() throws Exception {
        String jsonPayload = String.format(
                "{\"portfolioId\":\"%s\",\"symbol\":\"AAPL\",\"exchange\":\"NASDAQ\",\"positionType\":\"LONG\",\"quantity\":100,\"entryPrice\":150.00}",
                samplePortfolioId
        );

        when(positionService.createPosition(any(CreatePositionRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Position created successfully"))
                .andExpect(jsonPath("$.data.symbol").value("AAPL"))
                .andExpect(jsonPath("$.data.exchange").value("NASDAQ"))
                .andExpect(jsonPath("$.data.currentPrice").value(150.00))
                .andExpect(jsonPath("$.data.unrealizedPnL").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/positions - Validation Failure (Missing Symbol & Negative Quantity)")
    void testCreatePosition_ValidationFailure() throws Exception {
        String invalidJsonPayload = String.format(
                "{\"portfolioId\":\"%s\",\"exchange\":\"NASDAQ\",\"positionType\":\"LONG\",\"quantity\":-10,\"entryPrice\":150.00}",
                samplePortfolioId
        );

        mockMvc.perform(post("/api/v1/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("GET /api/v1/positions - Success Scenario")
    void testGetAllPositions_Success() throws Exception {
        when(positionService.getAllPositions()).thenReturn(Collections.singletonList(sampleResponse));

        mockMvc.perform(get("/api/v1/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(samplePositionId.toString()))
                .andExpect(jsonPath("$.data[0].symbol").value("AAPL"));
    }

    @Test
    @DisplayName("GET /api/v1/positions/{id} - Success Scenario")
    void testGetPositionById_Success() throws Exception {
        when(positionService.getPositionById(eq(samplePositionId))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/positions/{id}", samplePositionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(samplePositionId.toString()))
                .andExpect(jsonPath("$.data.symbol").value("AAPL"));
    }

    @Test
    @DisplayName("GET /api/v1/positions/{id} - Not Found Scenario")
    void testGetPositionById_NotFound() throws Exception {
        when(positionService.getPositionById(eq(samplePositionId)))
                .thenThrow(new ResourceNotFoundException("Position", "id", samplePositionId));

        mockMvc.perform(get("/api/v1/positions/{id}", samplePositionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/positions/portfolio/{portfolioId} - Success Scenario")
    void testGetPositionsByPortfolioId_Success() throws Exception {
        when(positionService.getPositionsByPortfolioId(eq(samplePortfolioId)))
                .thenReturn(Collections.singletonList(sampleResponse));

        mockMvc.perform(get("/api/v1/positions/portfolio/{portfolioId}", samplePortfolioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].symbol").value("AAPL"));
    }

    @Test
    @DisplayName("PUT /api/v1/positions/{id} - Success Scenario")
    void testUpdatePosition_Success() throws Exception {
        String updatePayload = "{\"currentPrice\":165.50,\"status\":\"OPEN\"}";

        PositionResponse updatedResponse = PositionResponse.builder()
                .id(samplePositionId)
                .portfolioId(samplePortfolioId)
                .symbol("AAPL")
                .exchange(Exchange.NASDAQ)
                .positionType(PositionType.LONG)
                .quantity(BigDecimal.valueOf(100))
                .entryPrice(BigDecimal.valueOf(150.00))
                .currentPrice(BigDecimal.valueOf(165.50))
                .unrealizedPnL(BigDecimal.valueOf(1550.00))
                .status(PositionStatus.OPEN)
                .build();

        when(positionService.updatePosition(eq(samplePositionId), any(UpdatePositionRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/positions/{id}", samplePositionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentPrice").value(165.50))
                .andExpect(jsonPath("$.data.unrealizedPnL").value(1550.00));
    }

    @Test
    @DisplayName("DELETE /api/v1/positions/{id} - Success Scenario")
    void testDeletePosition_Success() throws Exception {
        doNothing().when(positionService).deletePosition(eq(samplePositionId));

        mockMvc.perform(delete("/api/v1/positions/{id}", samplePositionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Position deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/positions/{id} - Not Found Scenario")
    void testDeletePosition_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Position", "id", samplePositionId))
                .when(positionService).deletePosition(eq(samplePositionId));

        mockMvc.perform(delete("/api/v1/positions/{id}", samplePositionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}

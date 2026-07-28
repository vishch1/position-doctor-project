package com.vishakha.position_doctor_project.domain.alert.controller;

import com.vishakha.position_doctor_project.common.dto.AlertSeverity;
import com.vishakha.position_doctor_project.common.exception.GlobalExceptionHandler;
import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.domain.alert.dto.AlertResponse;
import com.vishakha.position_doctor_project.domain.alert.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AlertController alertController;

    private UUID sampleAlertId;
    private UUID sampleUserId;
    private AlertResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleAlertId = UUID.randomUUID();
        sampleUserId = UUID.randomUUID();

        sampleResponse = AlertResponse.builder()
                .id(sampleAlertId)
                .userId(sampleUserId)
                .portfolioId(UUID.randomUUID())
                .positionId(UUID.randomUUID())
                .title("Alert: AAPL Recommendation Changed to TIGHTEN_STOPLOSS")
                .message("Position health score shifted to 55/100")
                .severity(AlertSeverity.WARNING)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/alerts - Success Scenario")
    void testGetAllAlerts_Success() throws Exception {
        when(alertService.getAllAlerts()).thenReturn(Collections.singletonList(sampleResponse));

        mockMvc.perform(get("/api/v1/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Alert: AAPL Recommendation Changed to TIGHTEN_STOPLOSS"));
    }

    @Test
    @DisplayName("GET /api/v1/alerts/{userId} - Success Scenario")
    void testGetAlertsByUserId_Success() throws Exception {
        when(alertService.getAlertsByUserId(eq(sampleUserId))).thenReturn(Collections.singletonList(sampleResponse));

        mockMvc.perform(get("/api/v1/alerts/{userId}", sampleUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value(sampleUserId.toString()));
    }

    @Test
    @DisplayName("PUT /api/v1/alerts/{alertId}/read - Success Scenario")
    void testMarkAlertAsRead_Success() throws Exception {
        AlertResponse readResponse = AlertResponse.builder()
                .id(sampleAlertId)
                .userId(sampleUserId)
                .title("Alert: AAPL Recommendation Changed to TIGHTEN_STOPLOSS")
                .severity(AlertSeverity.WARNING)
                .read(true)
                .build();

        when(alertService.markAlertAsRead(eq(sampleAlertId))).thenReturn(readResponse);

        mockMvc.perform(put("/api/v1/alerts/{alertId}/read", sampleAlertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.read").value(true))
                .andExpect(jsonPath("$.message").value("Alert marked as read"));
    }

    @Test
    @DisplayName("PUT /api/v1/alerts/{alertId}/read - Not Found Scenario")
    void testMarkAlertAsRead_NotFound() throws Exception {
        when(alertService.markAlertAsRead(eq(sampleAlertId)))
                .thenThrow(new ResourceNotFoundException("Alert", "id", sampleAlertId));

        mockMvc.perform(put("/api/v1/alerts/{alertId}/read", sampleAlertId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}

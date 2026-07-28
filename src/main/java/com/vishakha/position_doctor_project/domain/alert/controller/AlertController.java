package com.vishakha.position_doctor_project.domain.alert.controller;

import com.vishakha.position_doctor_project.common.dto.ApiResponse;
import com.vishakha.position_doctor_project.domain.alert.dto.AlertResponse;
import com.vishakha.position_doctor_project.domain.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Alert Engine management endpoints.
 */
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAllAlerts() {
        List<AlertResponse> response = alertService.getAllAlerts();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlertsByUserId(@PathVariable UUID userId) {
        List<AlertResponse> response = alertService.getAlertsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlertsByUserIdAlias(@PathVariable UUID userId) {
        List<AlertResponse> response = alertService.getAlertsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{alertId}/read")
    public ResponseEntity<ApiResponse<AlertResponse>> markAlertAsRead(@PathVariable UUID alertId) {
        AlertResponse response = alertService.markAlertAsRead(alertId);
        return ResponseEntity.ok(ApiResponse.success("Alert marked as read", response));
    }
}

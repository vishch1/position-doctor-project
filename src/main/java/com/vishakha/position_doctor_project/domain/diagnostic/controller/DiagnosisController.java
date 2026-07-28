package com.vishakha.position_doctor_project.domain.diagnostic.controller;

import com.vishakha.position_doctor_project.common.dto.ApiResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller for Position Diagnosis Engine endpoints.
 */
@RestController
@RequestMapping("/api/v1/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @GetMapping("/{positionId}")
    public ResponseEntity<ApiResponse<PositionHealthReport>> getDiagnosis(@PathVariable UUID positionId) {
        PositionHealthReport report = diagnosisService.getPositionHealthReport(positionId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}

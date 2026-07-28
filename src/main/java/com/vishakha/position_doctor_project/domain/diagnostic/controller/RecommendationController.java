package com.vishakha.position_doctor_project.domain.diagnostic.controller;

import com.vishakha.position_doctor_project.common.dto.ApiResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller for Recommendation Engine endpoints.
 */
@RestController
@RequestMapping("/api/v1/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/{positionId}")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendation(@PathVariable UUID positionId) {
        RecommendationResponse response = recommendationService.getRecommendation(positionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

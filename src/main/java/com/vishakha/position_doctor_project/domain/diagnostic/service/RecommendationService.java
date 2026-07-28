package com.vishakha.position_doctor_project.domain.diagnostic.service;

import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;

import java.util.UUID;

/**
 * Service interface for fetching position actionable recommendations.
 */
public interface RecommendationService {

    RecommendationResponse getRecommendation(UUID positionId);
}

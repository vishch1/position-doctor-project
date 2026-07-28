package com.vishakha.position_doctor_project.domain.diagnostic.service;

import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.engine.RecommendationEngine;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementation for position recommendation generation.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final PositionRepository positionRepository;
    private final DiagnosisService diagnosisService;
    private final RecommendationEngine recommendationEngine;

    @Override
    public RecommendationResponse getRecommendation(UUID positionId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", positionId));

        PositionHealthReport healthReport = diagnosisService.getPositionHealthReport(positionId);

        return recommendationEngine.generateRecommendation(position, healthReport.getHealthScore());
    }
}

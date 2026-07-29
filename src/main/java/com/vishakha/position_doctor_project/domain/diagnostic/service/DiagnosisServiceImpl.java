package com.vishakha.position_doctor_project.domain.diagnostic.service;

import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReportDto;
import com.vishakha.position_doctor_project.domain.diagnostic.engine.DiagnosisEngine;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for position diagnosis processing using DiagnosisEngine as single source of truth.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosisServiceImpl implements DiagnosisService {

    private final PositionRepository positionRepository;
    private final DiagnosisEngine diagnosisEngine;

    @Override
    public PositionHealthReport getPositionHealthReport(UUID positionId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", positionId));

        PositionHealthReport report = diagnosisEngine.generateReport(position);

        // Persist the calculated RiskLevel back into Position entity
        positionRepository.save(position);

        return report;
    }

    @Override
    public PositionHealthReportDto diagnosePosition(String positionIdStr) {
        UUID positionId;
        try {
            positionId = UUID.fromString(positionIdStr);
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Position", "id", positionIdStr);
        }

        PositionHealthReport report = getPositionHealthReport(positionId);
        return mapToReportDto(report);
    }

    @Override
    public List<PositionHealthReportDto> diagnosePortfolioPositions(String portfolioIdStr) {
        UUID portfolioId;
        try {
            portfolioId = UUID.fromString(portfolioIdStr);
        } catch (Exception ex) {
            return Collections.emptyList();
        }

        List<Position> positions = positionRepository.findByPortfolioId(portfolioId);
        return positions.stream()
                .map(pos -> {
                    PositionHealthReport report = diagnosisEngine.generateReport(pos);
                    positionRepository.save(pos);
                    return mapToReportDto(report);
                })
                .collect(Collectors.toList());
    }

    private PositionHealthReportDto mapToReportDto(PositionHealthReport report) {
        return PositionHealthReportDto.builder()
                .reportId(UUID.randomUUID().toString())
                .positionId(report.getPositionId().toString())
                .healthScore(report.getHealthScore())
                .overallRiskLevel(report.getRiskLevel())
                .recommendations(List.of(report.getRecommendation().name() + ": " + report.getReason()))
                .warningFlags(Collections.emptyList())
                .generatedAt(LocalDateTime.now())
                .build();
    }
}

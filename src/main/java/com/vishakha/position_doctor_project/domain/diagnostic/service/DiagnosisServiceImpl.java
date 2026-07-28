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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for position diagnosis processing.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosisServiceImpl implements DiagnosisService {

    private final PositionRepository positionRepository;
    private final DiagnosisEngine diagnosisEngine;

    @Override
    public PositionHealthReport getPositionHealthReport(UUID positionId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", positionId));
        return diagnosisEngine.generateReport(position);
    }

    @Override
    public PositionHealthReportDto diagnosePosition(String positionId) {
        return null;
    }

    @Override
    public List<PositionHealthReportDto> diagnosePortfolioPositions(String portfolioId) {
        return Collections.emptyList();
    }
}

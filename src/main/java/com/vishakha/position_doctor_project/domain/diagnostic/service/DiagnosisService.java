package com.vishakha.position_doctor_project.domain.diagnostic.service;

import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReportDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Position Doctor diagnostic engine.
 */
public interface DiagnosisService {

    PositionHealthReport getPositionHealthReport(UUID positionId);

    PositionHealthReportDto diagnosePosition(String positionId);

    List<PositionHealthReportDto> diagnosePortfolioPositions(String portfolioId);
}

package com.vishakha.position_doctor_project.domain.diagnostic.service;

import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReportDto;

import java.util.List;

/**
 * Service interface for Position Doctor diagnostic engine.
 */
public interface PositionDoctorService {

    PositionHealthReportDto diagnosePosition(String positionId);

    List<PositionHealthReportDto> diagnosePortfolioPositions(String portfolioId);
}

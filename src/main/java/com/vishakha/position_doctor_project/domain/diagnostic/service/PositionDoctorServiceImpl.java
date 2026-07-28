package com.vishakha.position_doctor_project.domain.diagnostic.service;

import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReportDto;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Placeholder implementation of PositionDoctorService.
 */
@Service
public class PositionDoctorServiceImpl implements PositionDoctorService {

    @Override
    public PositionHealthReportDto diagnosePosition(String positionId) {
        return null;
    }

    @Override
    public List<PositionHealthReportDto> diagnosePortfolioPositions(String portfolioId) {
        return Collections.emptyList();
    }
}

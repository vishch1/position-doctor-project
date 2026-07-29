package com.vishakha.position_doctor_project.domain.portfoliohistory.service;

import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioChartResponse;
import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioSnapshotDto;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for managing portfolio historical snapshots and analytics.
 */
public interface PortfolioHistoryService {

    void takeSnapshotsForActivePortfolios();

    List<PortfolioSnapshotDto> getHistory(UUID portfolioId, Integer days);

    PortfolioSnapshotDto getLatestSnapshot(UUID portfolioId);

    PortfolioChartResponse getChartData(UUID portfolioId, Integer days);
}

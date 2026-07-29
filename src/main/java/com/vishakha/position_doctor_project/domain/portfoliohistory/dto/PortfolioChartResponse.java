package com.vishakha.position_doctor_project.domain.portfoliohistory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response object combining snapshot time series and calculated analytics for chart rendering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioChartResponse {

    private UUID portfolioId;
    private List<PortfolioSnapshotDto> snapshots;
    private PortfolioAnalyticsDto analytics;
}

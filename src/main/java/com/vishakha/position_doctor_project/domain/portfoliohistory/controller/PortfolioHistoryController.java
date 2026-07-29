package com.vishakha.position_doctor_project.domain.portfoliohistory.controller;

import com.vishakha.position_doctor_project.common.dto.ApiResponse;
import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioChartResponse;
import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioSnapshotDto;
import com.vishakha.position_doctor_project.domain.portfoliohistory.service.PortfolioHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing Historical Portfolio Tracking endpoints.
 */
@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class PortfolioHistoryController {

    private final PortfolioHistoryService portfolioHistoryService;

    @GetMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<List<PortfolioSnapshotDto>>> getHistory(
            @PathVariable UUID portfolioId,
            @RequestParam(required = false) Integer days) {
        List<PortfolioSnapshotDto> history = portfolioHistoryService.getHistory(portfolioId, days);
        return ResponseEntity.ok(ApiResponse.success("Portfolio history retrieved successfully", history));
    }

    @GetMapping("/{portfolioId}/latest")
    public ResponseEntity<ApiResponse<PortfolioSnapshotDto>> getLatestSnapshot(
            @PathVariable UUID portfolioId) {
        PortfolioSnapshotDto latest = portfolioHistoryService.getLatestSnapshot(portfolioId);
        return ResponseEntity.ok(ApiResponse.success("Latest portfolio snapshot retrieved successfully", latest));
    }

    @GetMapping("/{portfolioId}/chart")
    public ResponseEntity<ApiResponse<PortfolioChartResponse>> getChartData(
            @PathVariable UUID portfolioId,
            @RequestParam(required = false) Integer days) {
        PortfolioChartResponse chartData = portfolioHistoryService.getChartData(portfolioId, days);
        return ResponseEntity.ok(ApiResponse.success("Portfolio chart data retrieved successfully", chartData));
    }
}

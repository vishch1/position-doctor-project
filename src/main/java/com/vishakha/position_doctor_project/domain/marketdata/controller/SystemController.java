package com.vishakha.position_doctor_project.domain.marketdata.controller;

import com.vishakha.position_doctor_project.common.dto.ApiResponse;
import com.vishakha.position_doctor_project.domain.marketdata.dto.MarketStatusDto;
import com.vishakha.position_doctor_project.domain.marketdata.service.MarketStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing system health and live market status endpoints.
 */
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemController {

    private final MarketStatusService marketStatusService;

    @GetMapping("/market-status")
    public ResponseEntity<ApiResponse<MarketStatusDto>> getMarketStatus() {
        MarketStatusDto status = marketStatusService.getMarketStatus();
        return ResponseEntity.ok(ApiResponse.success("Market status retrieved successfully", status));
    }
}

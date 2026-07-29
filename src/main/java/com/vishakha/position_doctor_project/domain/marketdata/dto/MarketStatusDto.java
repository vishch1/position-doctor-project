package com.vishakha.position_doctor_project.domain.marketdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Output DTO for market data provider health and status check endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketStatusDto {

    private String provider;
    private String connectionStatus; // "ONLINE" or "OFFLINE"
    private long latency;             // in milliseconds
    private boolean fallbackActive;
    private LocalDateTime lastUpdated;
}

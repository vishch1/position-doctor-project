package com.vishakha.position_doctor_project.domain.marketdata.service;

import com.vishakha.position_doctor_project.domain.marketdata.dto.MarketStatusDto;

/**
 * Service contract for tracking live market data status, latency, and fallback state.
 */
public interface MarketStatusService {

    MarketStatusDto getMarketStatus();

    void recordUpdate(String provider, boolean fallbackActive, long latencyMs);
}

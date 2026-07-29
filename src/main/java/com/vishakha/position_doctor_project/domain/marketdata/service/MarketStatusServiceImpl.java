package com.vishakha.position_doctor_project.domain.marketdata.service;

import com.vishakha.position_doctor_project.domain.marketdata.dto.MarketStatusDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service implementation for managing live market data status, latency metrics, and fallback tracking.
 */
@Service
public class MarketStatusServiceImpl implements MarketStatusService {

    private final AtomicReference<String> currentProvider = new AtomicReference<>("FINNHUB");
    private final AtomicBoolean fallbackActive = new AtomicBoolean(false);
    private final AtomicLong lastLatency = new AtomicLong(0L);
    private final AtomicReference<LocalDateTime> lastUpdated = new AtomicReference<>(LocalDateTime.now());

    @Override
    public MarketStatusDto getMarketStatus() {
        boolean isFallback = fallbackActive.get();
        String providerName = isFallback ? "MOCK" : currentProvider.get();
        String status = isFallback ? "OFFLINE" : "ONLINE";

        return MarketStatusDto.builder()
                .provider(providerName)
                .connectionStatus(status)
                .latency(lastLatency.get())
                .fallbackActive(isFallback)
                .lastUpdated(lastUpdated.get())
                .build();
    }

    @Override
    public void recordUpdate(String provider, boolean isFallback, long latencyMs) {
        if (provider != null && !provider.isBlank()) {
            this.currentProvider.set(provider.toUpperCase());
        }
        this.fallbackActive.set(isFallback);
        this.lastLatency.set(latencyMs);
        this.lastUpdated.set(LocalDateTime.now());
    }
}

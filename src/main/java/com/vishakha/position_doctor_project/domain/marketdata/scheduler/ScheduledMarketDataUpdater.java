package com.vishakha.position_doctor_project.domain.marketdata.scheduler;

import com.vishakha.position_doctor_project.domain.marketdata.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task runner executing background market price updates every 30 seconds for OPEN positions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledMarketDataUpdater {

    private final MarketDataService marketDataService;

    @Scheduled(fixedRate = 30000)
    public void executeMarketDataUpdate() {
        log.info("Starting scheduled 30-second market data update cycle...");
        try {
            marketDataService.updateOpenPositionsMarketData();
            log.info("Scheduled 30-second market data update cycle completed successfully.");
        } catch (Exception ex) {
            log.error("Error encountered during scheduled market data update cycle: {}", ex.getMessage(), ex);
        }
    }
}

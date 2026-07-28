package com.vishakha.position_doctor_project.domain.marketdata.provider;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock market data provider implementation for development and testing.
 * Generates random market fluctuations (±1% to ±3%) around the base price.
 */
@Slf4j
@Component
public class MockMarketDataProvider implements MarketDataProvider {

    @Override
    public BigDecimal fetchLatestPrice(String symbol, Exchange exchange, BigDecimal fallbackPrice) {
        if (fallbackPrice == null || fallbackPrice.compareTo(BigDecimal.ZERO) <= 0) {
            fallbackPrice = BigDecimal.valueOf(100.00);
        }

        // Random percentage change between -3.0% and +3.0%
        double percentageChange = ThreadLocalRandom.current().nextDouble(-3.0, 3.0);
        BigDecimal multiplier = BigDecimal.valueOf(1 + (percentageChange / 100.0));

        BigDecimal simulatedPrice = fallbackPrice.multiply(multiplier).setScale(4, RoundingMode.HALF_UP);

        log.debug("Mock Market Quote fetched for Symbol: {} ({}) | Base: {} | Change: {}% -> Simulated: {}",
                symbol, exchange, fallbackPrice, String.format("%.2f", percentageChange), simulatedPrice);

        return simulatedPrice;
    }
}

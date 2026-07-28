package com.vishakha.position_doctor_project.domain.marketdata.provider;

import com.vishakha.position_doctor_project.common.dto.Exchange;

import java.math.BigDecimal;

/**
 * Strategy interface for market data price providers.
 * Easily replaceable with live financial APIs (Kite, Polygon, Yahoo Finance, AlphaVantage).
 */
public interface MarketDataProvider {

    BigDecimal fetchLatestPrice(String symbol, Exchange exchange, BigDecimal fallbackPrice);
}

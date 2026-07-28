package com.vishakha.position_doctor_project.domain.marketdata.service;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.domain.marketdata.dto.MarketQuoteDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for fetching market data and updating position prices.
 */
public interface MarketDataService {

    MarketQuoteDto getQuote(String symbol);

    List<MarketQuoteDto> getQuotes(List<String> symbols);

    BigDecimal getLatestPrice(String symbol, Exchange exchange, BigDecimal currentPrice);

    void updateOpenPositionsMarketData();
}

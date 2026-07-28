package com.vishakha.position_doctor_project.domain.marketdata.client;

import com.vishakha.position_doctor_project.domain.marketdata.dto.MarketQuoteDto;

import java.util.List;
import java.util.Optional;

/**
 * Interface contract for external financial market data provider integration.
 */
public interface MarketDataClient {

    Optional<MarketQuoteDto> fetchLatestQuote(String symbol);

    List<MarketQuoteDto> fetchBatchQuotes(List<String> symbols);
}

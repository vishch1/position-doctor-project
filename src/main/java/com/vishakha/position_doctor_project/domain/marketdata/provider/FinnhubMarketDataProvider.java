package com.vishakha.position_doctor_project.domain.marketdata.provider;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.domain.marketdata.cache.MarketDataCache;
import com.vishakha.position_doctor_project.domain.marketdata.client.FinnhubMarketDataClient;
import com.vishakha.position_doctor_project.domain.marketdata.dto.MarketQuoteDto;
import com.vishakha.position_doctor_project.domain.marketdata.mapper.SymbolMapper;
import com.vishakha.position_doctor_project.domain.marketdata.service.MarketStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Primary MarketDataProvider delegating HTTP calls to FinnhubMarketDataClient with caching, symbol mapping,
 * structured logging, latency tracking, and seamless fallback to MockMarketDataProvider.
 */
@Slf4j
@Primary
@Component
public class FinnhubMarketDataProvider implements MarketDataProvider {

    private final FinnhubMarketDataClient finnhubMarketDataClient;
    private final MockMarketDataProvider mockMarketDataProvider;
    private final SymbolMapper symbolMapper;
    private final MarketDataCache marketDataCache;
    private final MarketStatusService marketStatusService;

    public FinnhubMarketDataProvider(
            FinnhubMarketDataClient finnhubMarketDataClient,
            MockMarketDataProvider mockMarketDataProvider,
            SymbolMapper symbolMapper,
            MarketDataCache marketDataCache,
            MarketStatusService marketStatusService) {
        this.finnhubMarketDataClient = finnhubMarketDataClient;
        this.mockMarketDataProvider = mockMarketDataProvider;
        this.symbolMapper = symbolMapper;
        this.marketDataCache = marketDataCache;
        this.marketStatusService = marketStatusService;
    }

    @Override
    public BigDecimal fetchLatestPrice(String symbol, Exchange exchange, BigDecimal fallbackPrice) {
        String ticker = symbolMapper.toFinnhubSymbol(symbol, exchange);
        String cacheKey = (exchange != null ? exchange.name() : "DEFAULT") + ":" + ticker;

        // Check 30-second TTL cache to suppress duplicate API requests
        Optional<BigDecimal> cachedPrice = marketDataCache.get(cacheKey);
        if (cachedPrice.isPresent()) {
            log.info("Market Quote Fetched (CACHE HIT) -> Provider: FINNHUB | Data Source: CACHE | Symbol: {} ({}) | Price: {}",
                    symbol, exchange, cachedPrice.get());
            return cachedPrice.get();
        }

        long startTime = System.currentTimeMillis();

        if (finnhubMarketDataClient.isApiKeyConfigured()) {
            Optional<MarketQuoteDto> quoteOpt = finnhubMarketDataClient.fetchLatestQuote(ticker);
            long latencyMs = System.currentTimeMillis() - startTime;

            if (quoteOpt.isPresent() && quoteOpt.get().getLastPrice() != null) {
                BigDecimal livePrice = quoteOpt.get().getLastPrice();
                marketDataCache.put(cacheKey, livePrice);
                marketStatusService.recordUpdate("FINNHUB", false, latencyMs);

                log.info("Market Quote Fetched -> Provider: FINNHUB | Data Source: LIVE | Symbol: {} ({}) | Ticker: {} | Prev Price: {} -> Latest Price: {} | Latency: {}ms",
                        symbol, exchange, ticker, fallbackPrice, livePrice, latencyMs);
                return livePrice;
            }
        }

        // Automatic fallback to MockMarketDataProvider if Finnhub API key is missing or calls fail
        long latencyMs = System.currentTimeMillis() - startTime;
        BigDecimal mockPrice = mockMarketDataProvider.fetchLatestPrice(symbol, exchange, fallbackPrice);
        marketDataCache.put(cacheKey, mockPrice);
        marketStatusService.recordUpdate("FINNHUB", true, latencyMs);

        log.info("Market Quote Fetched -> Provider: MOCK | Data Source: MOCK | Symbol: {} ({}) | Prev Price: {} -> Latest Price: {} | Latency: {}ms",
                symbol, exchange, fallbackPrice, mockPrice, latencyMs);

        return mockPrice;
    }

    public String formatTicker(String symbol, Exchange exchange) {
        return symbolMapper.toFinnhubSymbol(symbol, exchange);
    }
}

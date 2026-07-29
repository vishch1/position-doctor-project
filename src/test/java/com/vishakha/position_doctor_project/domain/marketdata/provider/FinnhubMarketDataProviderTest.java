package com.vishakha.position_doctor_project.domain.marketdata.provider;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.domain.marketdata.cache.MarketDataCache;
import com.vishakha.position_doctor_project.domain.marketdata.client.FinnhubMarketDataClient;
import com.vishakha.position_doctor_project.domain.marketdata.dto.MarketQuoteDto;
import com.vishakha.position_doctor_project.domain.marketdata.mapper.SymbolMapper;
import com.vishakha.position_doctor_project.domain.marketdata.service.MarketStatusService;
import com.vishakha.position_doctor_project.domain.marketdata.service.MarketStatusServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinnhubMarketDataProviderTest {

    @Mock
    private FinnhubMarketDataClient finnhubMarketDataClient;

    @Mock
    private MockMarketDataProvider mockMarketDataProvider;

    private SymbolMapper symbolMapper;
    private MarketDataCache marketDataCache;
    private MarketStatusService marketStatusService;
    private FinnhubMarketDataProvider provider;

    @BeforeEach
    void setUp() {
        symbolMapper = new SymbolMapper();
        marketDataCache = new MarketDataCache();
        marketStatusService = new MarketStatusServiceImpl();
        provider = new FinnhubMarketDataProvider(
                finnhubMarketDataClient,
                mockMarketDataProvider,
                symbolMapper,
                marketDataCache,
                marketStatusService
        );
    }

    @Test
    @DisplayName("fetchLatestPrice - Successfully fetches live quote via client")
    void testFetchLatestPrice_Success() {
        when(finnhubMarketDataClient.isApiKeyConfigured()).thenReturn(true);
        when(finnhubMarketDataClient.fetchLatestQuote("AAPL")).thenReturn(Optional.of(
                MarketQuoteDto.builder()
                        .symbol("AAPL")
                        .lastPrice(BigDecimal.valueOf(185.85))
                        .timestamp(LocalDateTime.now())
                        .build()
        ));

        BigDecimal price = provider.fetchLatestPrice("AAPL", Exchange.NASDAQ, BigDecimal.valueOf(100.00));

        assertEquals(0, BigDecimal.valueOf(185.85).compareTo(price));
    }

    @Test
    @DisplayName("fetchLatestPrice - Missing API key triggers MockMarketDataProvider fallback")
    void testFetchLatestPrice_MissingApiKey_Fallback() {
        when(finnhubMarketDataClient.isApiKeyConfigured()).thenReturn(false);

        when(mockMarketDataProvider.fetchLatestPrice(eq("AAPL"), eq(Exchange.NASDAQ), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(101.50));

        BigDecimal price = provider.fetchLatestPrice("AAPL", Exchange.NASDAQ, BigDecimal.valueOf(100.00));

        assertEquals(BigDecimal.valueOf(101.50), price);
        verify(mockMarketDataProvider).fetchLatestPrice(eq("AAPL"), eq(Exchange.NASDAQ), any(BigDecimal.class));
    }

    @Test
    @DisplayName("fetchLatestPrice - Empty client response triggers MockMarketDataProvider fallback")
    void testFetchLatestPrice_ApiFailure_Fallback() {
        when(finnhubMarketDataClient.isApiKeyConfigured()).thenReturn(true);
        when(finnhubMarketDataClient.fetchLatestQuote("TSLA")).thenReturn(Optional.empty());

        when(mockMarketDataProvider.fetchLatestPrice(eq("TSLA"), eq(Exchange.NASDAQ), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(210.00));

        BigDecimal price = provider.fetchLatestPrice("TSLA", Exchange.NASDAQ, BigDecimal.valueOf(200.00));

        assertEquals(BigDecimal.valueOf(210.00), price);
        verify(mockMarketDataProvider).fetchLatestPrice(eq("TSLA"), eq(Exchange.NASDAQ), any(BigDecimal.class));
    }
}

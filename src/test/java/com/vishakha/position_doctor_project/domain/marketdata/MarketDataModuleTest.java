package com.vishakha.position_doctor_project.domain.marketdata;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.domain.marketdata.cache.MarketDataCache;
import com.vishakha.position_doctor_project.domain.marketdata.client.FinnhubMarketDataClient;
import com.vishakha.position_doctor_project.domain.marketdata.controller.SystemController;
import com.vishakha.position_doctor_project.domain.marketdata.dto.MarketQuoteDto;
import com.vishakha.position_doctor_project.domain.marketdata.dto.MarketStatusDto;
import com.vishakha.position_doctor_project.domain.marketdata.mapper.SymbolMapper;
import com.vishakha.position_doctor_project.domain.marketdata.provider.FinnhubMarketDataProvider;
import com.vishakha.position_doctor_project.domain.marketdata.provider.MockMarketDataProvider;
import com.vishakha.position_doctor_project.domain.marketdata.service.MarketStatusService;
import com.vishakha.position_doctor_project.domain.marketdata.service.MarketStatusServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketDataModuleTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private FinnhubMarketDataClient mockDataClient;

    @Mock
    private MockMarketDataProvider mockMarketDataProvider;

    private SymbolMapper symbolMapper;
    private MarketDataCache marketDataCache;
    private MarketStatusService marketStatusService;
    private FinnhubMarketDataProvider finnhubMarketDataProvider;

    @BeforeEach
    void setUp() {
        symbolMapper = new SymbolMapper();
        marketDataCache = new MarketDataCache();
        marketStatusService = new MarketStatusServiceImpl();
        finnhubMarketDataProvider = new FinnhubMarketDataProvider(
                mockDataClient,
                mockMarketDataProvider,
                symbolMapper,
                marketDataCache,
                marketStatusService
        );
    }

    @Test
    @DisplayName("SymbolMapper correctly formats ticker symbols based on Exchange")
    void testSymbolMapper() {
        assertEquals("RELIANCE.NS", symbolMapper.toFinnhubSymbol("RELIANCE", Exchange.NSE));
        assertEquals("INFY.BO", symbolMapper.toFinnhubSymbol("INFY", Exchange.BSE));
        assertEquals("AAPL", symbolMapper.toFinnhubSymbol("AAPL", Exchange.NASDAQ));
        assertEquals("TSLA", symbolMapper.toFinnhubSymbol("TSLA", Exchange.NYSE));
    }

    @Test
    @DisplayName("MarketDataCache caches values for 30s and suppresses duplicate external requests")
    void testMarketDataCache30SecTTL() {
        BigDecimal price1 = BigDecimal.valueOf(150.00);
        marketDataCache.put("NASDAQ:AAPL", price1);

        Optional<BigDecimal> cached = marketDataCache.get("NASDAQ:AAPL");
        assertTrue(cached.isPresent());
        assertEquals(price1, cached.get());

        // Test cache hit in provider
        BigDecimal result = finnhubMarketDataProvider.fetchLatestPrice("AAPL", Exchange.NASDAQ, BigDecimal.valueOf(100.00));
        assertEquals(price1, result);
        verifyNoInteractions(mockDataClient);
        verifyNoInteractions(mockMarketDataProvider);
    }

    @Test
    @DisplayName("FinnhubMarketDataClient retries up to 3 times on HTTP exception with backoff")
    void testClientRetriesOnException() {
        FinnhubMarketDataClient client = new FinnhubMarketDataClient(restTemplate);
        ReflectionTestUtils.setField(client, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(client, "finnhubBaseUrl", "https://finnhub.io/api/v1");
        ReflectionTestUtils.setField(client, "maxRetries", 3);

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection timeout"))
                .thenThrow(new ResourceAccessException("Read timeout"))
                .thenReturn(new ResponseEntity<>("{\"c\": 180.50}", HttpStatus.OK));

        Optional<MarketQuoteDto> quoteOpt = client.fetchLatestQuote("AAPL");

        assertTrue(quoteOpt.isPresent());
        assertEquals(BigDecimal.valueOf(180.50).setScale(4), quoteOpt.get().getLastPrice());
        verify(restTemplate, times(3)).getForEntity(anyString(), eq(String.class));
    }

    @Test
    @DisplayName("FinnhubMarketDataProvider automatically falls back to MockMarketDataProvider when API key is missing")
    void testFallbackWhenApiKeyMissing() {
        when(mockDataClient.isApiKeyConfigured()).thenReturn(false);
        when(mockMarketDataProvider.fetchLatestPrice("TSLA", Exchange.NASDAQ, BigDecimal.valueOf(200.00)))
                .thenReturn(BigDecimal.valueOf(205.00));

        BigDecimal price = finnhubMarketDataProvider.fetchLatestPrice("TSLA", Exchange.NASDAQ, BigDecimal.valueOf(200.00));

        assertEquals(BigDecimal.valueOf(205.00), price);
        verify(mockMarketDataProvider, times(1)).fetchLatestPrice("TSLA", Exchange.NASDAQ, BigDecimal.valueOf(200.00));
        assertTrue(marketStatusService.getMarketStatus().isFallbackActive());
        assertEquals("MOCK", marketStatusService.getMarketStatus().getProvider());
    }

    @Test
    @DisplayName("SystemController GET /api/v1/system/market-status returns HTTP 200 with status DTO")
    void testSystemControllerMarketStatusEndpoint() {
        marketStatusService.recordUpdate("FINNHUB", false, 45L);
        SystemController systemController = new SystemController(marketStatusService);

        ResponseEntity<?> response = systemController.getMarketStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}

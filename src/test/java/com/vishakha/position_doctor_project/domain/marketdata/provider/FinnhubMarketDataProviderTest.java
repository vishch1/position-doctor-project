package com.vishakha.position_doctor_project.domain.marketdata.provider;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinnhubMarketDataProviderTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MockMarketDataProvider mockMarketDataProvider;

    @InjectMocks
    private FinnhubMarketDataProvider provider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(provider, "finnhubBaseUrl", "https://finnhub.io/api/v1");
        ReflectionTestUtils.setField(provider, "apiKey", "testApiKey123456");
        ReflectionTestUtils.setField(provider, "maxRetries", 2);
    }

    @Test
    @DisplayName("fetchLatestPrice - Successfully parses Finnhub quote 'c' (current price) JSON field")
    void testFetchLatestPrice_Success() {
        String mockResponseJson = "{\"c\": 185.85, \"d\": 2.1, \"dp\": 1.14, \"h\": 186.2, \"l\": 184.0, \"o\": 184.5, \"pc\": 183.75, \"t\": 1700000000}";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponseJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);

        BigDecimal price = provider.fetchLatestPrice("AAPL", Exchange.NASDAQ, BigDecimal.valueOf(100.00));

        assertEquals(0, BigDecimal.valueOf(185.85).compareTo(price));
    }

    @Test
    @DisplayName("fetchLatestPrice - Missing API key triggers MockMarketDataProvider fallback")
    void testFetchLatestPrice_MissingApiKey_Fallback() {
        ReflectionTestUtils.setField(provider, "apiKey", "");

        when(mockMarketDataProvider.fetchLatestPrice(eq("AAPL"), eq(Exchange.NASDAQ), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(101.50));

        BigDecimal price = provider.fetchLatestPrice("AAPL", Exchange.NASDAQ, BigDecimal.valueOf(100.00));

        assertEquals(BigDecimal.valueOf(101.50), price);
        verify(mockMarketDataProvider).fetchLatestPrice(eq("AAPL"), eq(Exchange.NASDAQ), any(BigDecimal.class));
    }

    @Test
    @DisplayName("fetchLatestPrice - RestTemplate exception triggers retry and MockMarketDataProvider fallback")
    void testFetchLatestPrice_ApiFailure_Fallback() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        when(mockMarketDataProvider.fetchLatestPrice(eq("TSLA"), eq(Exchange.NASDAQ), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(210.00));

        BigDecimal price = provider.fetchLatestPrice("TSLA", Exchange.NASDAQ, BigDecimal.valueOf(200.00));

        assertEquals(BigDecimal.valueOf(210.00), price);
        verify(mockMarketDataProvider).fetchLatestPrice(eq("TSLA"), eq(Exchange.NASDAQ), any(BigDecimal.class));
    }
}

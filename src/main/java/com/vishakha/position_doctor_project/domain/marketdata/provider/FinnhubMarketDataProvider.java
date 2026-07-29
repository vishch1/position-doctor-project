package com.vishakha.position_doctor_project.domain.marketdata.provider;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Live market data provider implementation querying Finnhub API v1 quote endpoint.
 * Pluggable strategy with automatic fallback to MockMarketDataProvider on API network failure, missing API key, or throttling.
 */
@Slf4j
@Primary
@Component
public class FinnhubMarketDataProvider implements MarketDataProvider {

    private final RestTemplate restTemplate;
    private final MockMarketDataProvider mockMarketDataProvider;
    private final ObjectMapper objectMapper;

    @Value("${app.market-data.finnhub.base-url:https://finnhub.io/api/v1}")
    private String finnhubBaseUrl;

    @Value("${app.market-data.finnhub.api-key:}")
    private String apiKey;

    @Value("${app.market-data.max-retries:3}")
    private int maxRetries;

    public FinnhubMarketDataProvider(
            RestTemplate restTemplate,
            MockMarketDataProvider mockMarketDataProvider) {
        this.restTemplate = restTemplate;
        this.mockMarketDataProvider = mockMarketDataProvider;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public BigDecimal fetchLatestPrice(String symbol, Exchange exchange, BigDecimal fallbackPrice) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Finnhub API key is not configured. Falling back to MockMarketDataProvider.");
            return mockMarketDataProvider.fetchLatestPrice(symbol, exchange, fallbackPrice);
        }

        String ticker = formatTicker(symbol, exchange);
        String url = String.format("%s/quote?symbol=%s&token=%s", finnhubBaseUrl, ticker, apiKey);

        int attempt = 0;
        while (attempt < maxRetries) {
            attempt++;
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    BigDecimal livePrice = extractPriceFromJson(response.getBody());
                    if (livePrice != null && livePrice.compareTo(BigDecimal.ZERO) > 0) {
                        log.info("Live Finnhub Market Quote fetched -> Symbol: {} ({}) | Ticker: {} -> Current Price: {}",
                                symbol, exchange, ticker, livePrice);
                        return livePrice;
                    }
                }
            } catch (Exception ex) {
                log.warn("Attempt {}/{} failed fetching Finnhub market price for Ticker {}: {}",
                        attempt, maxRetries, ticker, ex.getMessage());
                try {
                    Thread.sleep(100); // 100ms backoff before retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.warn("Failed fetching Finnhub market price for Symbol: {} ({}) after {} attempts. Falling back to MockMarketDataProvider.",
                symbol, exchange, maxRetries);
        return mockMarketDataProvider.fetchLatestPrice(symbol, exchange, fallbackPrice);
    }

    public String formatTicker(String symbol, Exchange exchange) {
        if (symbol == null || symbol.isBlank()) {
            return "AAPL";
        }
        return symbol.trim().toUpperCase();
    }

    private BigDecimal extractPriceFromJson(String jsonResponseBody) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponseBody);
            if (root.has("c")) {
                double currentPrice = root.path("c").asDouble();
                if (currentPrice > 0) {
                    return BigDecimal.valueOf(currentPrice).setScale(4, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception ex) {
            log.error("Failed parsing Finnhub quote JSON response: {}", ex.getMessage());
        }
        return null;
    }
}

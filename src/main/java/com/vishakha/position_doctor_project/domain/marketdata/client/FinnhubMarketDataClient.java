package com.vishakha.position_doctor_project.domain.marketdata.client;

import com.vishakha.position_doctor_project.domain.marketdata.dto.MarketQuoteDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Dedicated HTTP client for communicating with the Finnhub API v1 endpoints.
 * Handles timeouts, response parsing, and up to 3 retries with exponential backoff.
 */
@Slf4j
@Component
public class FinnhubMarketDataClient implements MarketDataClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.market-data.finnhub.base-url:https://finnhub.io/api/v1}")
    private String finnhubBaseUrl;

    @Value("${app.market-data.finnhub.api-key:${FINNHUB_API_KEY:}}")
    private String apiKey;

    @Value("${app.market-data.max-retries:3}")
    private int maxRetries;

    public FinnhubMarketDataClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public Optional<MarketQuoteDto> fetchLatestQuote(String symbol) {
        if (!isApiKeyConfigured()) {
            log.warn("Finnhub API key is missing or blank.");
            return Optional.empty();
        }

        String ticker = symbol != null && !symbol.isBlank() ? symbol.trim().toUpperCase() : "AAPL";
        String url = String.format("%s/quote?symbol=%s&token=%s", finnhubBaseUrl, ticker, apiKey);

        int attempt = 0;
        long backoffMs = 100;

        while (attempt < maxRetries) {
            attempt++;
            long startTime = System.currentTimeMillis();
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                long latency = System.currentTimeMillis() - startTime;

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    BigDecimal livePrice = extractPriceFromJson(response.getBody());
                    if (livePrice != null && livePrice.compareTo(BigDecimal.ZERO) > 0) {
                        return Optional.of(MarketQuoteDto.builder()
                                .symbol(ticker)
                                .lastPrice(livePrice)
                                .timestamp(LocalDateTime.now())
                                .build());
                    }
                }
            } catch (Exception ex) {
                long latency = System.currentTimeMillis() - startTime;
                log.warn("Attempt {}/{} failed fetching Finnhub quote for symbol [{}] (Latency: {}ms): {}",
                        attempt, maxRetries, ticker, latency, ex.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(backoffMs);
                        backoffMs *= 2; // Exponential backoff: 100ms, 200ms, 400ms
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.warn("Exhausted {} retry attempts for Finnhub quote symbol [{}]", maxRetries, ticker);
        return Optional.empty();
    }

    @Override
    public List<MarketQuoteDto> fetchBatchQuotes(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }
        return symbols.stream()
                .map(this::fetchLatestQuote)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
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
            log.error("Error parsing Finnhub quote JSON response: {}", ex.getMessage());
        }
        return null;
    }
}

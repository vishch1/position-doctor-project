package com.vishakha.position_doctor_project.domain.marketdata.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe cache with a 30-second TTL for market data quotes to prevent duplicate external API requests.
 */
@Component
public class MarketDataCache {

    private static final long DEFAULT_TTL_SECONDS = 30;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long ttlSeconds;

    public MarketDataCache() {
        this(DEFAULT_TTL_SECONDS);
    }

    public MarketDataCache(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public Optional<BigDecimal> get(String cacheKey) {
        if (cacheKey == null || cacheKey.isBlank()) {
            return Optional.empty();
        }

        CacheEntry entry = cache.get(cacheKey);
        if (entry == null) {
            return Optional.empty();
        }

        if (Instant.now().isAfter(entry.getExpiresAt())) {
            cache.remove(cacheKey);
            return Optional.empty();
        }

        return Optional.of(entry.getPrice());
    }

    public void put(String cacheKey, BigDecimal price) {
        if (cacheKey != null && !cacheKey.isBlank() && price != null) {
            Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);
            cache.put(cacheKey, new CacheEntry(price, expiresAt));
        }
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    @Getter
    @AllArgsConstructor
    private static class CacheEntry {
        private final BigDecimal price;
        private final Instant expiresAt;
    }
}

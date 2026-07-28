package com.vishakha.position_doctor_project.domain.marketdata.service;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.util.FinancialCalculatorUtils;
import com.vishakha.position_doctor_project.domain.marketdata.dto.MarketQuoteDto;
import com.vishakha.position_doctor_project.domain.marketdata.provider.MarketDataProvider;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing live market price updates and batch position re-evaluations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MarketDataServiceImpl implements MarketDataService {

    private final MarketDataProvider marketDataProvider;
    private final PositionRepository positionRepository;

    @Override
    @Transactional(readOnly = true)
    public MarketQuoteDto getQuote(String symbol) {
        BigDecimal price = marketDataProvider.fetchLatestPrice(symbol, null, BigDecimal.valueOf(100.00));
        return MarketQuoteDto.builder()
                .symbol(symbol)
                .lastPrice(price)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketQuoteDto> getQuotes(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }
        return symbols.stream()
                .map(this::getQuote)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getLatestPrice(String symbol, Exchange exchange, BigDecimal currentPrice) {
        return marketDataProvider.fetchLatestPrice(symbol, exchange, currentPrice);
    }

    @Override
    public void updateOpenPositionsMarketData() {
        List<Position> openPositions = positionRepository.findByStatus(PositionStatus.OPEN);

        if (openPositions.isEmpty()) {
            log.debug("No OPEN positions found for market data update.");
            return;
        }

        log.info("Processing market price update for {} OPEN position(s)...", openPositions.size());

        for (Position position : openPositions) {
            BigDecimal oldPrice = position.getCurrentPrice() != null ? position.getCurrentPrice() : position.getEntryPrice();
            BigDecimal newPrice = marketDataProvider.fetchLatestPrice(position.getSymbol(), position.getExchange(), oldPrice);

            position.setCurrentPrice(newPrice);

            BigDecimal updatedPnL = FinancialCalculatorUtils.calculateUnrealizedPnL(
                    position.getEntryPrice(),
                    newPrice,
                    position.getQuantity()
            );
            position.setUnrealizedPnL(updatedPnL);

            log.info("Market Update -> Position [{}] Symbol: {} ({}) | Old Price: {} -> New Price: {} | Unrealized PnL: {}",
                    position.getId(), position.getSymbol(), position.getExchange(), oldPrice, newPrice, updatedPnL);
        }

        positionRepository.saveAll(openPositions);
        log.info("Successfully updated and saved {} OPEN position(s).", openPositions.size());
    }
}

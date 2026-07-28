package com.vishakha.position_doctor_project.domain.marketdata.service;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.dto.PositionType;
import com.vishakha.position_doctor_project.domain.marketdata.provider.MarketDataProvider;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.position.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketDataServiceTest {

    @Mock
    private MarketDataProvider marketDataProvider;

    @Mock
    private PositionRepository positionRepository;

    @InjectMocks
    private MarketDataServiceImpl marketDataService;

    private Position sampleOpenPosition;

    @BeforeEach
    void setUp() {
        sampleOpenPosition = Position.builder()
                .id(UUID.randomUUID())
                .symbol("AAPL")
                .exchange(Exchange.NASDAQ)
                .positionType(PositionType.LONG)
                .quantity(BigDecimal.valueOf(10))
                .entryPrice(BigDecimal.valueOf(100.00))
                .currentPrice(BigDecimal.valueOf(100.00))
                .unrealizedPnL(BigDecimal.ZERO)
                .status(PositionStatus.OPEN)
                .build();
    }

    @Test
    @DisplayName("updateOpenPositionsMarketData - Successfully updates currentPrice and unrealizedPnL for open positions")
    void testUpdateOpenPositionsMarketData_Success() {
        when(positionRepository.findByStatus(PositionStatus.OPEN))
                .thenReturn(Collections.singletonList(sampleOpenPosition));

        // Simulated price increases from 100.00 to 110.00 (+10.00 per share * 10 shares = +100.00 PnL)
        when(marketDataProvider.fetchLatestPrice(eq("AAPL"), eq(Exchange.NASDAQ), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(110.00));

        marketDataService.updateOpenPositionsMarketData();

        assertEquals(0, BigDecimal.valueOf(110.00).compareTo(sampleOpenPosition.getCurrentPrice()));
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(sampleOpenPosition.getUnrealizedPnL()));

        verify(positionRepository).saveAll(any());
    }

    @Test
    @DisplayName("updateOpenPositionsMarketData - No open positions found")
    void testUpdateOpenPositionsMarketData_NoOpenPositions() {
        when(positionRepository.findByStatus(PositionStatus.OPEN))
                .thenReturn(Collections.emptyList());

        marketDataService.updateOpenPositionsMarketData();

        verify(positionRepository, never()).saveAll(any());
    }
}

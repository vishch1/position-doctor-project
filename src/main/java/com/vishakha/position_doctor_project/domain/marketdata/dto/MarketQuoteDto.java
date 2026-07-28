package com.vishakha.position_doctor_project.domain.marketdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Market data price quote DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketQuoteDto {

    private String symbol;
    private BigDecimal lastPrice;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private BigDecimal volume;
    private LocalDateTime timestamp;
}

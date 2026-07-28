package com.vishakha.position_doctor_project.domain.position.dto;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.common.dto.PositionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a new trading position.
 * Note: currentPrice is NOT accepted in creation request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePositionRequest {

    @NotNull(message = "Portfolio ID is required")
    private UUID portfolioId;

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Exchange is required")
    private Exchange exchange;

    @NotNull(message = "Position type is required")
    private PositionType positionType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Entry price is required")
    @Positive(message = "Entry price must be positive")
    private BigDecimal entryPrice;

    private BigDecimal stopLossPrice;

    private BigDecimal takeProfitPrice;
}

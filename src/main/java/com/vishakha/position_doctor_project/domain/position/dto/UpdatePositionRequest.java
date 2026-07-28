package com.vishakha.position_doctor_project.domain.position.dto;

import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.dto.PositionType;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing position.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePositionRequest {

    private Exchange exchange;

    private PositionType positionType;

    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @Positive(message = "Entry price must be positive")
    private BigDecimal entryPrice;

    @Positive(message = "Current price must be positive")
    private BigDecimal currentPrice;

    private BigDecimal stopLossPrice;

    private BigDecimal takeProfitPrice;

    private PositionStatus status;
}

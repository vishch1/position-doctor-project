package com.vishakha.position_doctor_project.domain.portfolio.dto;

import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Portfolio CRUD operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {

    private UUID id;
    private UUID userId;
    private String name;
    private String description;
    private BigDecimal totalValue;
    private BigDecimal totalUnrealizedPnL;
    private String currency;
    private RiskLevel aggregatedRiskLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.vishakha.position_doctor_project.domain.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new portfolio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePortfolioRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Portfolio name is required")
    private String name;

    private String description;

    private String currency;
}

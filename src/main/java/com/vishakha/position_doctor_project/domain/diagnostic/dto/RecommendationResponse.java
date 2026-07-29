package com.vishakha.position_doctor_project.domain.diagnostic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Output DTO for Actionable Position Recommendation with actionable next steps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private UUID positionId;
    private String symbol;
    private RecommendationType recommendation;
    private int confidence; // Range: 0 to 100
    private String reason;
    private List<String> actions;
}

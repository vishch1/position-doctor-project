package com.vishakha.position_doctor_project.common.dto;

/**
 * Lifecycle status of trading positions.
 */
public enum PositionStatus {
    OPEN,
    PARTIALLY_CLOSED,
    CLOSED,
    HEDGED,
    LIQUIDATION_PENDING
}

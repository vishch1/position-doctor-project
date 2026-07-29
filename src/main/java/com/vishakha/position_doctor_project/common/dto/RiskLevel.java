package com.vishakha.position_doctor_project.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Domain enum representing position risk assessment severity levels.
 * Guarantees that any legacy or unmapped value (such as UNKNOWN) defaults safely to MODERATE.
 */
public enum RiskLevel {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL;

    @JsonCreator
    public static RiskLevel fromValue(String value) {
        if (value == null || value.isBlank()) {
            return MODERATE;
        }
        for (RiskLevel level : values()) {
            if (level.name().equalsIgnoreCase(value.trim())) {
                return level;
            }
        }
        return MODERATE;
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}

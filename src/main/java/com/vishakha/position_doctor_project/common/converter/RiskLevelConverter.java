package com.vishakha.position_doctor_project.common.converter;

import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA AttributeConverter for RiskLevel enum.
 * Safely maps DB column values to RiskLevel, defaulting legacy or unrecognized values (such as 'UNKNOWN') to MODERATE.
 */
@Slf4j
@Converter(autoApply = true)
public class RiskLevelConverter implements AttributeConverter<RiskLevel, String> {

    @Override
    public String convertToDatabaseColumn(RiskLevel attribute) {
        if (attribute == null) {
            return RiskLevel.MODERATE.name();
        }
        return attribute.name();
    }

    @Override
    public RiskLevel convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return RiskLevel.MODERATE;
        }
        try {
            return RiskLevel.valueOf(dbData.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unrecognized or legacy RiskLevel value found in database column: '{}'. Safely converting to MODERATE.", dbData);
            return RiskLevel.MODERATE;
        }
    }
}

package com.vishakha.position_doctor_project.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility functions for precise financial calculation operations (PnL, Return %, Drawdown).
 */
public final class FinancialCalculatorUtils {

    private FinancialCalculatorUtils() {
        // Private constructor for utility class
    }

    public static BigDecimal calculateUnrealizedPnL(BigDecimal entryPrice, BigDecimal currentPrice, BigDecimal quantity) {
        if (entryPrice == null || currentPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return currentPrice.subtract(entryPrice).multiply(quantity).setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateReturnPercentage(BigDecimal entryPrice, BigDecimal currentPrice) {
        if (entryPrice == null || currentPrice == null || entryPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentPrice.subtract(entryPrice)
                .divide(entryPrice, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}

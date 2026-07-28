package com.vishakha.position_doctor_project.domain.diagnostic.engine;

import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.dto.PositionType;
import com.vishakha.position_doctor_project.common.util.FinancialCalculatorUtils;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Calculator component for evaluating financial position health score (range 0 to 100).
 */
@Component
public class HealthScoreCalculator {

    public int calculateHealthScore(Position position) {
        if (position == null) {
            return 0;
        }

        if (position.getStatus() != PositionStatus.OPEN) {
            return 50; // Neutral score for closed or non-open positions
        }

        int score = 70; // Baseline score for healthy open position

        BigDecimal entryPrice = position.getEntryPrice();
        BigDecimal currentPrice = position.getCurrentPrice() != null ? position.getCurrentPrice() : entryPrice;
        BigDecimal stopLoss = position.getStopLossPrice();
        BigDecimal takeProfit = position.getTakeProfitPrice();
        PositionType type = position.getPositionType() != null ? position.getPositionType() : PositionType.LONG;

        // 1. Unrealized PnL Factor (-30 to +20)
        BigDecimal returnPct = FinancialCalculatorUtils.calculateReturnPercentage(entryPrice, currentPrice);
        if (type == PositionType.SHORT) {
            returnPct = returnPct.negate();
        }

        double pnlVal = returnPct.doubleValue();
        if (pnlVal >= 20.0) {
            score += 20;
        } else if (pnlVal >= 5.0) {
            score += 10;
        } else if (pnlVal >= 0.0) {
            score += 5;
        } else if (pnlVal >= -5.0) {
            score -= 10;
        } else if (pnlVal >= -15.0) {
            score -= 20;
        } else {
            score -= 30;
        }

        // 2. Distance to Stop Loss Factor (-30 to +10)
        if (stopLoss != null && stopLoss.compareTo(BigDecimal.ZERO) > 0) {
            if (type == PositionType.LONG) {
                if (currentPrice.compareTo(stopLoss) <= 0) {
                    score -= 30; // Stop loss breached!
                } else {
                    double distToSLPct = currentPrice.subtract(stopLoss)
                            .divide(currentPrice, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    if (distToSLPct <= 2.0) {
                        score -= 20;
                    } else if (distToSLPct <= 5.0) {
                        score -= 10;
                    } else if (distToSLPct > 10.0) {
                        score += 5;
                    }
                }
            }
        } else {
            score -= 15; // Penalty for trading without stop loss
        }

        // 3. Distance to Take Profit Factor (0 to +15)
        if (takeProfit != null && takeProfit.compareTo(BigDecimal.ZERO) > 0) {
            if (type == PositionType.LONG && currentPrice.compareTo(takeProfit) >= 0) {
                score += 15; // Take profit target achieved
            }
        }

        // Clamp final score strictly between 0 and 100
        return Math.max(0, Math.min(100, score));
    }
}

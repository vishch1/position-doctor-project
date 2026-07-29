package com.vishakha.position_doctor_project.domain.diagnostic.engine;

import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationType;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Quant engine evaluating Position health scores and PnL metrics to generate actionable recommendations and next steps.
 */
@Component
public class RecommendationEngine {

    public RecommendationResponse generateRecommendation(Position position, int healthScore) {
        if (position == null) {
            return RecommendationResponse.builder()
                    .recommendation(RecommendationType.HOLD)
                    .confidence(0)
                    .reason("Position details not available.")
                    .actions(List.of("REFRESH_ANALYSIS", "CONTINUE_MONITORING"))
                    .build();
        }

        BigDecimal pnl = position.getUnrealizedPnL() != null ? position.getUnrealizedPnL() : BigDecimal.ZERO;
        RecommendationType action;
        int confidence;
        String reason;
        List<String> actions;

        if (healthScore > 80) {
            action = RecommendationType.HOLD;
            confidence = 95;
            reason = String.format("Position health score is excellent (%d/100). Strong risk-reward structure; maintaining current position is highly recommended.", healthScore);
            actions = List.of("REFRESH_ANALYSIS", "CONTINUE_MONITORING");
        } else if (healthScore >= 60) {
            action = RecommendationType.HOLD;
            confidence = 90;
            reason = String.format("Position health score is solid (%d/100). Market conditions favor holding current size.", healthScore);
            actions = List.of("REFRESH_ANALYSIS", "CONTINUE_MONITORING");
        } else if (healthScore >= 40) {
            action = RecommendationType.TIGHTEN_STOPLOSS;
            confidence = 80;
            reason = String.format("Position health score is moderate (%d/100). Tightening stop-loss threshold is recommended to protect against market volatility.", healthScore);
            actions = List.of("MODIFY_STOP_LOSS", "CONTINUE_MONITORING");
        } else if (healthScore >= 25) {
            if (pnl.compareTo(BigDecimal.ZERO) > 0) {
                action = RecommendationType.BOOK_PROFIT;
                confidence = 85;
                reason = String.format("Position health score is in the vulnerable 25-40 bracket (%d/100), but unrealized gains are available. Booking profit is recommended to secure capital.", healthScore);
                actions = List.of("BOOK_PROFIT", "SELL_50_PERCENT", "TRAIL_STOP_LOSS");
            } else {
                action = RecommendationType.ADD;
                confidence = 70;
                reason = String.format("Position health score is in the 25-40 bracket (%d/100) with drawdown. Dollar-cost averaging (adding to position) is recommended if core thesis remains sound.", healthScore);
                actions = List.of("BUY_MORE", "VIEW_PORTFOLIO");
            }
        } else {
            action = RecommendationType.EXIT;
            confidence = 95;
            reason = String.format("Position health score is critically low (%d/100). High risk of further capital erosion; immediate position exit is strongly advised.", healthScore);
            actions = List.of("SELL_POSITION", "VIEW_POSITION", "IGNORE");
        }

        return RecommendationResponse.builder()
                .positionId(position.getId())
                .symbol(position.getSymbol())
                .recommendation(action)
                .confidence(confidence)
                .reason(reason)
                .actions(actions)
                .build();
    }
}

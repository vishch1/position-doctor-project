package com.vishakha.position_doctor_project.domain.alert.engine;

import com.vishakha.position_doctor_project.common.dto.AlertSeverity;
import com.vishakha.position_doctor_project.domain.alert.entity.Alert;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationType;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Component for evaluating health score shifts and recommendation changes to trigger alerts.
 */
@Component
public class AlertEvaluator {

    public Optional<Alert> evaluateAlert(
            Position position,
            PositionHealthReport healthReport,
            RecommendationResponse recResponse,
            Optional<Alert> lastAlert) {

        if (position == null || healthReport == null || recResponse == null) {
            return Optional.empty();
        }

        int newScore = healthReport.getHealthScore();
        RecommendationType newRec = recResponse.getRecommendation();

        Integer oldScore = lastAlert.map(Alert::getNewHealthScore).orElse(null);
        RecommendationType oldRec = lastAlert.map(Alert::getNewRecommendation).orElse(null);

        boolean recChanged = oldRec != null && oldRec != newRec;
        boolean scoreShifted = oldScore != null && Math.abs(newScore - oldScore) >= 10;

        // If no prior alert, trigger initial alert if score is critical or recommendation is not HOLD
        if (lastAlert.isEmpty()) {
            if (newScore < 40 || newRec == RecommendationType.EXIT || newRec == RecommendationType.BOOK_PROFIT) {
                return Optional.of(buildAlert(position, null, newScore, null, newRec, true, true));
            }
            return Optional.empty();
        }

        if (recChanged || scoreShifted) {
            return Optional.of(buildAlert(position, oldScore, newScore, oldRec, newRec, recChanged, scoreShifted));
        }

        return Optional.empty();
    }

    private Alert buildAlert(
            Position position,
            Integer oldScore,
            int newScore,
            RecommendationType oldRec,
            RecommendationType newRec,
            boolean recChanged,
            boolean scoreShifted) {

        AlertSeverity severity;
        if (newRec == RecommendationType.EXIT || newScore < 35) {
            severity = AlertSeverity.CRITICAL;
        } else if (newRec == RecommendationType.TIGHTEN_STOPLOSS || (oldScore != null && oldScore - newScore >= 15)) {
            severity = AlertSeverity.WARNING;
        } else {
            severity = AlertSeverity.INFO;
        }

        String title;
        String message;

        if (recChanged) {
            title = String.format("Alert: %s Recommendation Changed to %s", position.getSymbol(), newRec);
            message = String.format("Position %s recommendation shifted from %s to %s. Reason: %s",
                    position.getSymbol(), oldRec != null ? oldRec : "INITIAL", newRec, position.getSymbol());
        } else {
            title = String.format("Alert: %s Health Score Shifted to %d/100", position.getSymbol(), newScore);
            message = String.format("Position %s health score shifted from %d to %d (Δ %d points).",
                    position.getSymbol(), oldScore != null ? oldScore : 70, newScore, oldScore != null ? newScore - oldScore : 0);
        }

        return Alert.builder()
                .user(position.getPortfolio().getUser())
                .portfolio(position.getPortfolio())
                .position(position)
                .title(title)
                .message(message)
                .severity(severity)
                .read(false)
                .previousHealthScore(oldScore)
                .newHealthScore(newScore)
                .previousRecommendation(oldRec)
                .newRecommendation(newRec)
                .build();
    }
}

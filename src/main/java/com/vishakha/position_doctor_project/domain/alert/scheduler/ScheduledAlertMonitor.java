package com.vishakha.position_doctor_project.domain.alert.scheduler;

import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.domain.alert.engine.AlertEvaluator;
import com.vishakha.position_doctor_project.domain.alert.entity.Alert;
import com.vishakha.position_doctor_project.domain.alert.repository.AlertRepository;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReport;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationResponse;
import com.vishakha.position_doctor_project.domain.diagnostic.service.DiagnosisService;
import com.vishakha.position_doctor_project.domain.diagnostic.service.RecommendationService;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Background scheduled monitor scanning OPEN positions every 30 seconds for health shifts & alerts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledAlertMonitor {

    private final PositionRepository positionRepository;
    private final DiagnosisService diagnosisService;
    private final RecommendationService recommendationService;
    private final AlertEvaluator alertEvaluator;
    private final AlertRepository alertRepository;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void monitorPositionsForAlerts() {
        List<Position> openPositions = positionRepository.findByStatus(PositionStatus.OPEN);

        if (openPositions.isEmpty()) {
            log.debug("Alert Monitor: No OPEN positions to evaluate.");
            return;
        }

        log.info("Alert Monitor: Scanning {} OPEN position(s) for alert conditions...", openPositions.size());

        for (Position position : openPositions) {
            try {
                PositionHealthReport healthReport = diagnosisService.getPositionHealthReport(position.getId());
                RecommendationResponse recResponse = recommendationService.getRecommendation(position.getId());

                Optional<Alert> lastAlert = alertRepository.findFirstByPositionIdOrderByCreatedAtDesc(position.getId());

                Optional<Alert> newAlert = alertEvaluator.evaluateAlert(position, healthReport, recResponse, lastAlert);

                if (newAlert.isPresent()) {
                    Alert savedAlert = alertRepository.save(newAlert.get());
                    log.info("Alert Triggered -> Position [{}] Symbol: {} | Severity: {} | Title: {}",
                            position.getId(), position.getSymbol(), savedAlert.getSeverity(), savedAlert.getTitle());
                }
            } catch (Exception ex) {
                log.error("Alert Monitor: Failed evaluating alert for Position [{}]: {}", position.getId(), ex.getMessage());
            }
        }
    }
}

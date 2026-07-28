package com.vishakha.position_doctor_project.domain.alert.service;

import com.vishakha.position_doctor_project.domain.alert.dto.AlertResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Alert notification management.
 */
public interface AlertService {

    List<AlertResponse> getAllAlerts();

    List<AlertResponse> getAlertsByUserId(UUID userId);

    AlertResponse markAlertAsRead(UUID alertId);
}

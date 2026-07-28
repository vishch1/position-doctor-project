package com.vishakha.position_doctor_project.domain.dashboard.service;

import com.vishakha.position_doctor_project.domain.dashboard.dto.DashboardResponse;

/**
 * Service interface for aggregating executive dashboard metrics.
 */
public interface DashboardService {

    DashboardResponse getDashboardSummary();
}

package com.vishakha.position_doctor_project.domain.portfolio.service;

import com.vishakha.position_doctor_project.domain.portfolio.dto.CreatePortfolioRequest;
import com.vishakha.position_doctor_project.domain.portfolio.dto.PortfolioResponse;
import com.vishakha.position_doctor_project.domain.portfolio.dto.UpdatePortfolioRequest;

import java.util.List;
import java.util.UUID;

/**
 * Service interface defining Portfolio CRUD operations.
 */
public interface PortfolioService {

    PortfolioResponse createPortfolio(CreatePortfolioRequest request);

    PortfolioResponse getPortfolioById(UUID id);

    List<PortfolioResponse> getAllPortfolios(UUID userId);

    PortfolioResponse updatePortfolio(UUID id, UpdatePortfolioRequest request);

    void deletePortfolio(UUID id);
}

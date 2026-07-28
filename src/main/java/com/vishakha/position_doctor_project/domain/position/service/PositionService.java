package com.vishakha.position_doctor_project.domain.position.service;

import com.vishakha.position_doctor_project.domain.position.dto.CreatePositionRequest;
import com.vishakha.position_doctor_project.domain.position.dto.PositionResponse;
import com.vishakha.position_doctor_project.domain.position.dto.UpdatePositionRequest;

import java.util.List;
import java.util.UUID;

/**
 * Service interface defining Position CRUD operations.
 */
public interface PositionService {

    PositionResponse createPosition(CreatePositionRequest request);

    PositionResponse getPositionById(UUID id);

    List<PositionResponse> getPositionsByPortfolioId(UUID portfolioId);

    PositionResponse updatePosition(UUID id, UpdatePositionRequest request);

    void deletePosition(UUID id);
}

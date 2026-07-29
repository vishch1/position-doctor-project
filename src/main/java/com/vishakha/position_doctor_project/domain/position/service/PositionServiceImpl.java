package com.vishakha.position_doctor_project.domain.position.service;

import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.common.util.FinancialCalculatorUtils;
import com.vishakha.position_doctor_project.domain.portfolio.entity.Portfolio;
import com.vishakha.position_doctor_project.domain.portfolio.repository.PortfolioRepository;
import com.vishakha.position_doctor_project.domain.position.dto.CreatePositionRequest;
import com.vishakha.position_doctor_project.domain.position.dto.PositionResponse;
import com.vishakha.position_doctor_project.domain.position.dto.UpdatePositionRequest;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import com.vishakha.position_doctor_project.domain.alert.repository.AlertRepository;
import com.vishakha.position_doctor_project.domain.diagnostic.engine.DiagnosisEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Position CRUD management.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final PortfolioRepository portfolioRepository;
    private final AlertRepository alertRepository;
    private final DiagnosisEngine diagnosisEngine;

    @Override
    public PositionResponse createPosition(CreatePositionRequest request) {
        Portfolio portfolio = portfolioRepository.findById(request.getPortfolioId())
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", request.getPortfolioId()));

        Position position = Position.builder()
                .portfolio(portfolio)
                .symbol(request.getSymbol().toUpperCase().trim())
                .exchange(request.getExchange())
                .positionType(request.getPositionType())
                .quantity(request.getQuantity())
                .entryPrice(request.getEntryPrice())
                .currentPrice(request.getEntryPrice()) // Rule: currentPrice = entryPrice on creation
                .unrealizedPnL(BigDecimal.ZERO)        // Rule: unrealizedPnL = 0 on creation
                .stopLossPrice(request.getStopLossPrice())
                .takeProfitPrice(request.getTakeProfitPrice())
                .status(PositionStatus.OPEN)            // Rule: status = OPEN on creation
                .riskLevel(RiskLevel.MODERATE)
                .build();

        // Single Source of Truth Risk Evaluation via DiagnosisEngine
        diagnosisEngine.generateReport(position);

        Position savedPosition = positionRepository.save(position);
        return mapToResponse(savedPosition);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionResponse getPositionById(UUID id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));
        return mapToResponse(position);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponse> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponse> getPositionsByPortfolioId(UUID portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new ResourceNotFoundException("Portfolio", "id", portfolioId);
        }

        return positionRepository.findByPortfolioId(portfolioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PositionResponse updatePosition(UUID id, UpdatePositionRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));

        if (request.getExchange() != null) {
            position.setExchange(request.getExchange());
        }
        if (request.getPositionType() != null) {
            position.setPositionType(request.getPositionType());
        }
        if (request.getQuantity() != null) {
            position.setQuantity(request.getQuantity());
        }
        if (request.getEntryPrice() != null) {
            position.setEntryPrice(request.getEntryPrice());
        }
        if (request.getCurrentPrice() != null) {
            position.setCurrentPrice(request.getCurrentPrice());
        }
        if (request.getStopLossPrice() != null) {
            position.setStopLossPrice(request.getStopLossPrice());
        }
        if (request.getTakeProfitPrice() != null) {
            position.setTakeProfitPrice(request.getTakeProfitPrice());
        }
        if (request.getStatus() != null) {
            position.setStatus(request.getStatus());
        }

        // Recalculate unrealized PnL based on current and entry price
        if (position.getEntryPrice() != null && position.getCurrentPrice() != null && position.getQuantity() != null) {
            position.setUnrealizedPnL(
                    FinancialCalculatorUtils.calculateUnrealizedPnL(
                            position.getEntryPrice(),
                            position.getCurrentPrice(),
                            position.getQuantity()
                    )
            );
        }

        // Single Source of Truth Risk Evaluation via DiagnosisEngine
        diagnosisEngine.generateReport(position);

        Position updatedPosition = positionRepository.save(position);
        return mapToResponse(updatedPosition);
    }

    @Override
    public void deletePosition(UUID id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position", "id", id));
        alertRepository.deleteByPositionId(id);
        positionRepository.delete(position);
    }

    private PositionResponse mapToResponse(Position position) {
        RiskLevel risk = position.getRiskLevel() != null ? position.getRiskLevel() : RiskLevel.MODERATE;
        return PositionResponse.builder()
                .id(position.getId())
                .portfolioId(position.getPortfolio() != null ? position.getPortfolio().getId() : null)
                .symbol(position.getSymbol())
                .exchange(position.getExchange())
                .positionType(position.getPositionType())
                .quantity(position.getQuantity())
                .entryPrice(position.getEntryPrice())
                .currentPrice(position.getCurrentPrice())
                .unrealizedPnL(position.getUnrealizedPnL())
                .stopLossPrice(position.getStopLossPrice())
                .takeProfitPrice(position.getTakeProfitPrice())
                .status(position.getStatus())
                .riskLevel(risk)
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }
}

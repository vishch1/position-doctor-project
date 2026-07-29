package com.vishakha.position_doctor_project.domain.portfolio.service;

import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.common.util.SecurityUtils;
import com.vishakha.position_doctor_project.domain.portfolio.dto.CreatePortfolioRequest;
import com.vishakha.position_doctor_project.domain.portfolio.dto.PortfolioResponse;
import com.vishakha.position_doctor_project.domain.portfolio.dto.UpdatePortfolioRequest;
import com.vishakha.position_doctor_project.domain.portfolio.entity.Portfolio;
import com.vishakha.position_doctor_project.domain.portfolio.repository.PortfolioRepository;
import com.vishakha.position_doctor_project.domain.user.entity.User;
import com.vishakha.position_doctor_project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vishakha.position_doctor_project.domain.alert.repository.AlertRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Portfolio CRUD operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final AlertRepository alertRepository;

    @Override
    public PortfolioResponse createPortfolio(CreatePortfolioRequest request) {
        String userEmail = SecurityUtils.getCurrentUserLogin().orElse(null);

        User user = null;
        if (userEmail != null && !userEmail.isBlank()) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        if (user == null) {
            user = userRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("User", "authenticatedUser", "current"));
        }

        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .currency(request.getCurrency() != null && !request.getCurrency().isBlank() ? request.getCurrency() : "USD")
                .totalValue(BigDecimal.ZERO)
                .totalUnrealizedPnL(BigDecimal.ZERO)
                .aggregatedRiskLevel(RiskLevel.MODERATE)
                .build();

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return mapToResponse(savedPortfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioById(UUID id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", id));
        return mapToResponse(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getAllPortfolios(UUID userId) {
        List<Portfolio> portfolios = (userId != null)
                ? portfolioRepository.findByUserId(userId)
                : portfolioRepository.findAll();

        return portfolios.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioResponse updatePortfolio(UUID id, UpdatePortfolioRequest request) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", id));

        portfolio.setName(request.getName());
        if (request.getDescription() != null) {
            portfolio.setDescription(request.getDescription());
        }
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            portfolio.setCurrency(request.getCurrency());
        }

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        return mapToResponse(updatedPortfolio);
    }

    @Override
    public void deletePortfolio(UUID id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", id));
        alertRepository.deleteByPortfolioId(id);
        portfolioRepository.delete(portfolio);
    }

    private PortfolioResponse mapToResponse(Portfolio portfolio) {
        RiskLevel risk = portfolio.getAggregatedRiskLevel() != null ? portfolio.getAggregatedRiskLevel() : RiskLevel.MODERATE;
        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .userId(portfolio.getUser() != null ? portfolio.getUser().getId() : null)
                .name(portfolio.getName())
                .description(portfolio.getDescription())
                .totalValue(portfolio.getTotalValue())
                .totalUnrealizedPnL(portfolio.getTotalUnrealizedPnL())
                .currency(portfolio.getCurrency())
                .aggregatedRiskLevel(risk)
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }
}

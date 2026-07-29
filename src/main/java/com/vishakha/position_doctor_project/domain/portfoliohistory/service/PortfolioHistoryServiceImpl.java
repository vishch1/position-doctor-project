package com.vishakha.position_doctor_project.domain.portfoliohistory.service;

import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.PositionHealthReportDto;
import com.vishakha.position_doctor_project.domain.diagnostic.service.DiagnosisService;
import com.vishakha.position_doctor_project.domain.portfolio.entity.Portfolio;
import com.vishakha.position_doctor_project.domain.portfolio.repository.PortfolioRepository;
import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioAnalyticsDto;
import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioChartResponse;
import com.vishakha.position_doctor_project.domain.portfoliohistory.dto.PortfolioSnapshotDto;
import com.vishakha.position_doctor_project.domain.portfoliohistory.entity.PortfolioSnapshot;
import com.vishakha.position_doctor_project.domain.portfoliohistory.repository.PortfolioSnapshotRepository;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for recording historical portfolio snapshots and computing performance analytics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioHistoryServiceImpl implements PortfolioHistoryService {

    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final DiagnosisService diagnosisService;

    @Override
    public void takeSnapshotsForActivePortfolios() {
        List<Portfolio> portfolios = portfolioRepository.findAll();
        if (portfolios.isEmpty()) {
            log.debug("No portfolios available for snapshot creation.");
            return;
        }

        log.info("Generating historical snapshots for {} portfolio(s)...", portfolios.size());

        for (Portfolio portfolio : portfolios) {
            try {
                createSnapshotForPortfolio(portfolio);
            } catch (Exception ex) {
                log.error("Failed to generate historical snapshot for Portfolio [{}]: {}",
                        portfolio.getId(), ex.getMessage(), ex);
            }
        }
    }

    private void createSnapshotForPortfolio(Portfolio portfolio) {
        List<Position> positions = positionRepository.findByPortfolioId(portfolio.getId());
        List<Position> openPositions = positions.stream()
                .filter(p -> p.getStatus() == PositionStatus.OPEN)
                .collect(Collectors.toList());

        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal portfolioValue = BigDecimal.ZERO;

        for (Position p : openPositions) {
            BigDecimal qty = p.getQuantity() != null ? p.getQuantity() : BigDecimal.ZERO;
            BigDecimal entryPrice = p.getEntryPrice() != null ? p.getEntryPrice() : BigDecimal.ZERO;
            BigDecimal currentPrice = p.getCurrentPrice() != null ? p.getCurrentPrice() : entryPrice;

            totalInvestment = totalInvestment.add(entryPrice.multiply(qty));
            portfolioValue = portfolioValue.add(currentPrice.multiply(qty));
        }

        if (openPositions.isEmpty() && portfolio.getTotalValue() != null) {
            portfolioValue = portfolio.getTotalValue();
        }

        final BigDecimal finalPortfolioValue = portfolioValue;
        BigDecimal unrealizedPnL = finalPortfolioValue.subtract(totalInvestment);

        int avgHealthScore = 100;
        try {
            List<PositionHealthReportDto> reports = diagnosisService.diagnosePortfolioPositions(portfolio.getId().toString());
            if (reports != null && !reports.isEmpty()) {
                double avg = reports.stream()
                        .mapToInt(PositionHealthReportDto::getHealthScore)
                        .average()
                        .orElse(100.0);
                avgHealthScore = (int) Math.round(avg);
            }
        } catch (Exception ex) {
            log.warn("Could not calculate health score for Portfolio [{}] during snapshot: {}", portfolio.getId(), ex.getMessage());
        }

        Optional<PortfolioSnapshot> lastSnapshotOpt = snapshotRepository.findFirstByPortfolioIdOrderBySnapshotTimeDesc(portfolio.getId());
        BigDecimal dayPnL = lastSnapshotOpt.map(last -> finalPortfolioValue.subtract(last.getPortfolioValue()))
                .orElse(unrealizedPnL);

        PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                .portfolioId(portfolio.getId())
                .snapshotTime(LocalDateTime.now())
                .portfolioValue(portfolioValue.setScale(4, RoundingMode.HALF_UP))
                .totalInvestment(totalInvestment.setScale(4, RoundingMode.HALF_UP))
                .unrealizedPnL(unrealizedPnL.setScale(4, RoundingMode.HALF_UP))
                .healthScore(avgHealthScore)
                .openPositions(openPositions.size())
                .dayPnL(dayPnL.setScale(4, RoundingMode.HALF_UP))
                .build();

        snapshotRepository.save(snapshot);
        log.debug("Persisted snapshot for Portfolio [{}] -> Value: {}, HealthScore: {}", portfolio.getId(), portfolioValue, avgHealthScore);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioSnapshotDto> getHistory(UUID portfolioId, Integer days) {
        verifyPortfolioExists(portfolioId);

        List<PortfolioSnapshot> snapshots;
        if (days != null && days > 0) {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
            snapshots = snapshotRepository.findByPortfolioIdAndSnapshotTimeAfterOrderBySnapshotTimeAsc(portfolioId, cutoff);
        } else {
            snapshots = snapshotRepository.findByPortfolioIdOrderBySnapshotTimeAsc(portfolioId);
        }

        return snapshots.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioSnapshotDto getLatestSnapshot(UUID portfolioId) {
        verifyPortfolioExists(portfolioId);

        PortfolioSnapshot snapshot = snapshotRepository.findFirstByPortfolioIdOrderBySnapshotTimeDesc(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioSnapshot", "portfolioId", portfolioId));

        return mapToDto(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioChartResponse getChartData(UUID portfolioId, Integer days) {
        List<PortfolioSnapshotDto> snapshots = getHistory(portfolioId, days);
        PortfolioAnalyticsDto analytics = calculateAnalytics(snapshots);

        return PortfolioChartResponse.builder()
                .portfolioId(portfolioId)
                .snapshots(snapshots)
                .analytics(analytics)
                .build();
    }

    private void verifyPortfolioExists(UUID portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new ResourceNotFoundException("Portfolio", "id", portfolioId);
        }
    }

    private PortfolioAnalyticsDto calculateAnalytics(List<PortfolioSnapshotDto> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return PortfolioAnalyticsDto.builder()
                    .highestValue(BigDecimal.ZERO)
                    .lowestValue(BigDecimal.ZERO)
                    .maxDrawdown(BigDecimal.ZERO)
                    .maxDrawdownPercent(0.0)
                    .averageDailyReturn(0.0)
                    .bestDayPnL(BigDecimal.ZERO)
                    .worstDayPnL(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal highestValue = snapshots.get(0).getPortfolioValue();
        BigDecimal lowestValue = snapshots.get(0).getPortfolioValue();
        BigDecimal bestDayPnL = snapshots.get(0).getDayPnL() != null ? snapshots.get(0).getDayPnL() : BigDecimal.ZERO;
        BigDecimal worstDayPnL = snapshots.get(0).getDayPnL() != null ? snapshots.get(0).getDayPnL() : BigDecimal.ZERO;

        BigDecimal peak = BigDecimal.ZERO;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        double maxDrawdownPercent = 0.0;

        double sumReturns = 0.0;
        int returnCount = 0;

        for (int i = 0; i < snapshots.size(); i++) {
            PortfolioSnapshotDto s = snapshots.get(i);
            BigDecimal val = s.getPortfolioValue() != null ? s.getPortfolioValue() : BigDecimal.ZERO;

            if (val.compareTo(highestValue) > 0) {
                highestValue = val;
            }
            if (val.compareTo(lowestValue) < 0) {
                lowestValue = val;
            }

            BigDecimal dayPnL = s.getDayPnL() != null ? s.getDayPnL() : BigDecimal.ZERO;
            if (dayPnL.compareTo(bestDayPnL) > 0) {
                bestDayPnL = dayPnL;
            }
            if (dayPnL.compareTo(worstDayPnL) < 0) {
                worstDayPnL = dayPnL;
            }

            // Max Drawdown calculation
            if (val.compareTo(peak) > 0) {
                peak = val;
            }
            if (peak.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal drawdown = peak.subtract(val);
                if (drawdown.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = drawdown;
                    maxDrawdownPercent = (drawdown.doubleValue() / peak.doubleValue()) * 100.0;
                }
            }

            // Daily return calculation between consecutive snapshots
            if (i > 0) {
                BigDecimal prevVal = snapshots.get(i - 1).getPortfolioValue();
                if (prevVal != null && prevVal.compareTo(BigDecimal.ZERO) > 0) {
                    double ret = ((val.doubleValue() - prevVal.doubleValue()) / prevVal.doubleValue()) * 100.0;
                    sumReturns += ret;
                    returnCount++;
                }
            }
        }

        double avgDailyReturn = returnCount > 0 ? sumReturns / returnCount : 0.0;

        return PortfolioAnalyticsDto.builder()
                .highestValue(highestValue)
                .lowestValue(lowestValue)
                .maxDrawdown(maxDrawdown)
                .maxDrawdownPercent(Math.round(maxDrawdownPercent * 100.0) / 100.0)
                .averageDailyReturn(Math.round(avgDailyReturn * 100.0) / 100.0)
                .bestDayPnL(bestDayPnL)
                .worstDayPnL(worstDayPnL)
                .build();
    }

    private PortfolioSnapshotDto mapToDto(PortfolioSnapshot snapshot) {
        return PortfolioSnapshotDto.builder()
                .id(snapshot.getId())
                .portfolioId(snapshot.getPortfolioId())
                .snapshotTime(snapshot.getSnapshotTime())
                .portfolioValue(snapshot.getPortfolioValue())
                .totalInvestment(snapshot.getTotalInvestment())
                .unrealizedPnL(snapshot.getUnrealizedPnL())
                .healthScore(snapshot.getHealthScore())
                .openPositions(snapshot.getOpenPositions())
                .dayPnL(snapshot.getDayPnL())
                .build();
    }
}

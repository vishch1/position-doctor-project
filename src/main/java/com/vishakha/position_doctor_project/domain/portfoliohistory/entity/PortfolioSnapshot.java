package com.vishakha.position_doctor_project.domain.portfoliohistory.entity;

import com.vishakha.position_doctor_project.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity capturing a point-in-time financial and health snapshot of a portfolio.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "portfolio_snapshots",
    indexes = {
        @Index(name = "idx_snapshot_portfolio_id", columnList = "portfolio_id"),
        @Index(name = "idx_snapshot_time", columnList = "snapshot_time")
    }
)
public class PortfolioSnapshot extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @NotNull
    @Column(name = "snapshot_time", nullable = false)
    private LocalDateTime snapshotTime;

    @NotNull
    @Column(name = "portfolio_value", precision = 19, scale = 4, nullable = false)
    private BigDecimal portfolioValue;

    @NotNull
    @Column(name = "total_investment", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalInvestment;

    @NotNull
    @Column(name = "unrealized_pnl", precision = 19, scale = 4, nullable = false)
    private BigDecimal unrealizedPnL;

    @Column(name = "health_score", nullable = false)
    private int healthScore;

    @Column(name = "open_positions", nullable = false)
    private int openPositions;

    @Column(name = "day_pnl", precision = 19, scale = 4)
    private BigDecimal dayPnL;
}

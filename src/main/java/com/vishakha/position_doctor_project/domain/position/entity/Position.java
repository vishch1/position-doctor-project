package com.vishakha.position_doctor_project.domain.position.entity;

import com.vishakha.position_doctor_project.common.converter.RiskLevelConverter;
import com.vishakha.position_doctor_project.common.dto.Exchange;
import com.vishakha.position_doctor_project.common.dto.PositionStatus;
import com.vishakha.position_doctor_project.common.dto.PositionType;
import com.vishakha.position_doctor_project.common.dto.RiskLevel;
import com.vishakha.position_doctor_project.common.entity.BaseAuditEntity;
import com.vishakha.position_doctor_project.domain.alert.entity.Alert;
import com.vishakha.position_doctor_project.domain.portfolio.entity.Portfolio;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Position entity representing individual financial asset holdings.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "positions",
    indexes = {
        @Index(name = "idx_position_portfolio_id", columnList = "portfolio_id"),
        @Index(name = "idx_position_symbol", columnList = "symbol")
    }
)
public class Position extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @NotBlank
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange", length = 20)
    private Exchange exchange;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_type", length = 20)
    private PositionType positionType;

    @NotNull
    @Column(name = "quantity", precision = 19, scale = 8, nullable = false)
    private BigDecimal quantity;

    @NotNull
    @Column(name = "entry_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal entryPrice;

    @Column(name = "current_price", precision = 19, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "unrealized_pnl", precision = 19, scale = 4)
    private BigDecimal unrealizedPnL;

    @Column(name = "stop_loss_price", precision = 19, scale = 4)
    private BigDecimal stopLossPrice;

    @Column(name = "take_profit_price", precision = 19, scale = 4)
    private BigDecimal takeProfitPrice;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PositionStatus status = PositionStatus.OPEN;

    @Convert(converter = RiskLevelConverter.class)
    @Column(name = "risk_level", length = 30)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.MODERATE;

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Alert> alerts = new ArrayList<>();
}

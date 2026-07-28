package com.vishakha.position_doctor_project.domain.alert.entity;

import com.vishakha.position_doctor_project.common.dto.AlertSeverity;
import com.vishakha.position_doctor_project.common.entity.BaseAuditEntity;
import com.vishakha.position_doctor_project.domain.diagnostic.dto.RecommendationType;
import com.vishakha.position_doctor_project.domain.portfolio.entity.Portfolio;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.user.entity.User;
import jakarta.persistence.Column;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Alert entity representing automated health score and recommendation change notifications.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "alerts",
    indexes = {
        @Index(name = "idx_alert_user_id", columnList = "user_id"),
        @Index(name = "idx_alert_read_status", columnList = "read_status")
    }
)
public class Alert extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @NotBlank
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @NotBlank
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private AlertSeverity severity;

    @Column(name = "read_status", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "previous_health_score")
    private Integer previousHealthScore;

    @Column(name = "new_health_score")
    private Integer newHealthScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_recommendation", length = 30)
    private RecommendationType previousRecommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_recommendation", length = 30)
    private RecommendationType newRecommendation;
}

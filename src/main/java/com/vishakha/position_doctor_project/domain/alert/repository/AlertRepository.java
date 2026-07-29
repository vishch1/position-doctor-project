package com.vishakha.position_doctor_project.domain.alert.repository;

import com.vishakha.position_doctor_project.domain.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository interface for Alert entity.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Alert> findAllByOrderByCreatedAtDesc();

    Optional<Alert> findFirstByPositionIdOrderByCreatedAtDesc(UUID positionId);

    void deleteByPortfolioId(UUID portfolioId);

    void deleteByPositionId(UUID positionId);
}

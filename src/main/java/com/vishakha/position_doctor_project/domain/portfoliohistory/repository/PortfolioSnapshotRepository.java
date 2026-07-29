package com.vishakha.position_doctor_project.domain.portfoliohistory.repository;

import com.vishakha.position_doctor_project.domain.portfoliohistory.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing PortfolioSnapshot persistence.
 */
@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, UUID> {

    List<PortfolioSnapshot> findByPortfolioIdOrderBySnapshotTimeAsc(UUID portfolioId);

    List<PortfolioSnapshot> findByPortfolioIdAndSnapshotTimeAfterOrderBySnapshotTimeAsc(UUID portfolioId, LocalDateTime since);

    Optional<PortfolioSnapshot> findFirstByPortfolioIdOrderBySnapshotTimeDesc(UUID portfolioId);
}

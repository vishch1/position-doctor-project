package com.vishakha.position_doctor_project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Startup migration runner to clean up legacy persisted 'UNKNOWN' risk levels in the database.
 * Updates all records with risk_level = 'UNKNOWN' or NULL to 'MODERATE'.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseRiskLevelMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            int posUpdated = jdbcTemplate.update(
                    "UPDATE positions SET risk_level = 'MODERATE' WHERE risk_level = 'UNKNOWN' OR risk_level IS NULL"
            );
            int portUpdated = jdbcTemplate.update(
                    "UPDATE portfolios SET aggregated_risk_level = 'MODERATE' WHERE aggregated_risk_level = 'UNKNOWN' OR aggregated_risk_level IS NULL"
            );

            if (posUpdated > 0 || portUpdated > 0) {
                log.info("Database Cleanup Completed: Converted {} position(s) and {} portfolio(s) from UNKNOWN to MODERATE risk level.",
                        posUpdated, portUpdated);
            } else {
                log.info("Database Cleanup Check: All position and portfolio risk levels are up to date.");
            }
        } catch (Exception e) {
            log.warn("Database Risk Level Cleanup encountered an exception (non-fatal): {}", e.getMessage());
        }
    }
}

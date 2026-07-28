package com.vishakha.position_doctor_project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA Auditing for tracking creation and modification timestamps.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}

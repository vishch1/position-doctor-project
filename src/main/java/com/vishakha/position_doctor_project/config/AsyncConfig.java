package com.vishakha.position_doctor_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Task executor configuration for async financial position diagnostics and processing.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "financialTaskExecutor")
    public Executor financialTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("PositionDoctor-Async-");
        executor.initialize();
        return executor;
    }
}

package com.vishakha.position_doctor_project.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Type-safe configuration properties for Position Doctor application.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Security security = new Security();
    private MarketData marketData = new MarketData();
    private RiskEngine riskEngine = new RiskEngine();

    @Getter
    @Setter
    public static class Security {
        private String jwtSecret = "defaultSecretKeyForPositionDoctorFintechPlatformChangeInProd123456";
        private long jwtExpirationMs = 86400000L; // 24 hours
    }

    @Getter
    @Setter
    public static class MarketData {
        private String providerUrl;
        private String apiKey;
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 10000;
    }

    @Getter
    @Setter
    public static class RiskEngine {
        private double defaultMaxDrawdownPercent = 15.0;
        private double defaultStopLossThreshold = 5.0;
        private int calculationBatchSize = 100;
    }
}

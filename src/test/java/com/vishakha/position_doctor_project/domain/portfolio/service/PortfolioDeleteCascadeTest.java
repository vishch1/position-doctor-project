package com.vishakha.position_doctor_project.domain.portfolio.service;

import com.vishakha.position_doctor_project.domain.alert.entity.Alert;
import com.vishakha.position_doctor_project.domain.alert.repository.AlertRepository;
import com.vishakha.position_doctor_project.domain.portfolio.entity.Portfolio;
import com.vishakha.position_doctor_project.domain.portfolio.repository.PortfolioRepository;
import com.vishakha.position_doctor_project.domain.position.entity.Position;
import com.vishakha.position_doctor_project.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioDeleteCascadeTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    @Test
    @DisplayName("Deleting portfolio cleanly removes dependent alerts and positions without foreign key errors")
    void testDeletePortfolioRemovesAlertsAndPositions() {
        UUID portfolioId = UUID.randomUUID();
        User mockUser = User.builder().id(UUID.randomUUID()).build();
        Portfolio mockPortfolio = Portfolio.builder()
                .id(portfolioId)
                .user(mockUser)
                .positions(new ArrayList<>())
                .alerts(new ArrayList<>())
                .build();

        Position mockPosition = Position.builder()
                .id(UUID.randomUUID())
                .portfolio(mockPortfolio)
                .alerts(new ArrayList<>())
                .build();
        mockPortfolio.getPositions().add(mockPosition);

        Alert mockAlert = Alert.builder()
                .id(UUID.randomUUID())
                .portfolio(mockPortfolio)
                .position(mockPosition)
                .build();
        mockPortfolio.getAlerts().add(mockAlert);
        mockPosition.getAlerts().add(mockAlert);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(mockPortfolio));
        doNothing().when(alertRepository).deleteByPortfolioId(portfolioId);
        doNothing().when(portfolioRepository).delete(mockPortfolio);

        assertDoesNotThrow(() -> portfolioService.deletePortfolio(portfolioId));

        verify(alertRepository, times(1)).deleteByPortfolioId(portfolioId);
        verify(portfolioRepository, times(1)).delete(mockPortfolio);
    }
}

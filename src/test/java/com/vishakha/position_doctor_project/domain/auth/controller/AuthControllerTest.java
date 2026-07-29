package com.vishakha.position_doctor_project.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vishakha.position_doctor_project.common.dto.UserRole;
import com.vishakha.position_doctor_project.common.exception.GlobalExceptionHandler;
import com.vishakha.position_doctor_project.domain.auth.dto.AuthResponse;
import com.vishakha.position_doctor_project.domain.auth.dto.LoginRequest;
import com.vishakha.position_doctor_project.domain.auth.dto.RegisterRequest;
import com.vishakha.position_doctor_project.domain.auth.dto.UserResponse;
import com.vishakha.position_doctor_project.domain.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UserResponse sampleUserResponse;
    private AuthResponse sampleAuthResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        sampleUserResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .email("satoshi@positiondoctor.com")
                .firstName("Satoshi")
                .lastName("Nakamoto")
                .role(UserRole.ROLE_USER)
                .enabled(true)
                .build();

        sampleAuthResponse = AuthResponse.builder()
                .accessToken("mocked.jwt.token")
                .tokenType("Bearer")
                .expiresIn(86400000)
                .user(sampleUserResponse)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Success (201 Created)")
    void testRegister_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("satoshi@positiondoctor.com")
                .password("securePass123")
                .firstName("Satoshi")
                .lastName("Nakamoto")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(sampleAuthResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.data.user.email").value("satoshi@positiondoctor.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Success (200 OK)")
    void testLogin_Success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("satoshi@positiondoctor.com")
                .password("securePass123")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(sampleAuthResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mocked.jwt.token"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Success (200 OK)")
    void testGetCurrentUser_Success() throws Exception {
        Principal mockPrincipal = () -> "satoshi@positiondoctor.com";
        when(authService.getCurrentUser(eq("satoshi@positiondoctor.com"))).thenReturn(sampleUserResponse);

        mockMvc.perform(get("/api/v1/auth/me").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("satoshi@positiondoctor.com"));
    }
}

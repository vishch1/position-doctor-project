package com.vishakha.position_doctor_project.domain.user.controller;

import com.vishakha.position_doctor_project.common.dto.UserRole;
import com.vishakha.position_doctor_project.common.exception.GlobalExceptionHandler;
import com.vishakha.position_doctor_project.common.exception.ResourceNotFoundException;
import com.vishakha.position_doctor_project.domain.user.dto.CreateUserRequest;
import com.vishakha.position_doctor_project.domain.user.dto.UpdateUserRequest;
import com.vishakha.position_doctor_project.domain.user.dto.UserResponse;
import com.vishakha.position_doctor_project.domain.user.service.UserService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UUID sampleUserId;
    private UserResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleUserId = UUID.randomUUID();

        sampleResponse = UserResponse.builder()
                .id(sampleUserId)
                .email("trader@positiondoctor.com")
                .firstName("Satoshi")
                .lastName("Nakamoto")
                .role(UserRole.ROLE_USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/users - Success Scenario")
    void testCreateUser_Success() throws Exception {
        String jsonPayload = "{\"email\":\"trader@positiondoctor.com\",\"password\":\"password123\",\"firstName\":\"Satoshi\",\"lastName\":\"Nakamoto\",\"role\":\"ROLE_USER\"}";

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.email").value("trader@positiondoctor.com"))
                .andExpect(jsonPath("$.data.firstName").value("Satoshi"));
    }

    @Test
    @DisplayName("POST /api/v1/users - Validation Failure (Invalid Email Format)")
    void testCreateUser_ValidationFailure() throws Exception {
        String invalidJsonPayload = "{\"email\":\"invalid-email\",\"password\":\"pass\",\"firstName\":\"First\",\"lastName\":\"Last\"}";

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Success Scenario")
    void testGetUserById_Success() throws Exception {
        when(userService.getUserById(eq(sampleUserId))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/users/{id}", sampleUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(sampleUserId.toString()))
                .andExpect(jsonPath("$.data.email").value("trader@positiondoctor.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Resource Not Found Scenario")
    void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(eq(sampleUserId)))
                .thenThrow(new ResourceNotFoundException("User", "id", sampleUserId));

        mockMvc.perform(get("/api/v1/users/{id}", sampleUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/users - Success Scenario")
    void testGetAllUsers_Success() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(sampleResponse));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("trader@positiondoctor.com"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} - Success Scenario")
    void testUpdateUser_Success() throws Exception {
        String updatePayload = "{\"email\":\"updated@positiondoctor.com\",\"firstName\":\"Hal\",\"lastName\":\"Finney\",\"role\":\"ROLE_ADMIN\",\"enabled\":true}";

        UserResponse updatedResponse = UserResponse.builder()
                .id(sampleUserId)
                .email("updated@positiondoctor.com")
                .firstName("Hal")
                .lastName("Finney")
                .role(UserRole.ROLE_ADMIN)
                .enabled(true)
                .build();

        when(userService.updateUser(eq(sampleUserId), any(UpdateUserRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/users/{id}", sampleUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("updated@positiondoctor.com"))
                .andExpect(jsonPath("$.data.firstName").value("Hal"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - Success Scenario")
    void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(eq(sampleUserId));

        mockMvc.perform(delete("/api/v1/users/{id}", sampleUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - Not Found Scenario")
    void testDeleteUser_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("User", "id", sampleUserId))
                .when(userService).deleteUser(eq(sampleUserId));

        mockMvc.perform(delete("/api/v1/users/{id}", sampleUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}

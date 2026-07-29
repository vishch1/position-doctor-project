package com.vishakha.position_doctor_project.domain.auth.service;

import com.vishakha.position_doctor_project.domain.auth.dto.AuthResponse;
import com.vishakha.position_doctor_project.domain.auth.dto.LoginRequest;
import com.vishakha.position_doctor_project.domain.auth.dto.RegisterRequest;
import com.vishakha.position_doctor_project.domain.auth.dto.UserResponse;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(String email);
}

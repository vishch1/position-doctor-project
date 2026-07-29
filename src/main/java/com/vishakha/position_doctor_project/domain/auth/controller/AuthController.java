package com.vishakha.position_doctor_project.domain.auth.controller;

import com.vishakha.position_doctor_project.common.dto.ApiResponse;
import com.vishakha.position_doctor_project.common.exception.BadRequestException;
import com.vishakha.position_doctor_project.domain.auth.dto.AuthResponse;
import com.vishakha.position_doctor_project.domain.auth.dto.LoginRequest;
import com.vishakha.position_doctor_project.domain.auth.dto.RegisterRequest;
import com.vishakha.position_doctor_project.domain.auth.dto.UserResponse;
import com.vishakha.position_doctor_project.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST Controller for JWT authentication operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("User authenticated successfully", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new BadRequestException("User is not authenticated");
        }
        UserResponse user = authService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}

package com.vishakha.position_doctor_project.domain.user.service;

import com.vishakha.position_doctor_project.domain.user.dto.CreateUserRequest;
import com.vishakha.position_doctor_project.domain.user.dto.UpdateUserRequest;
import com.vishakha.position_doctor_project.domain.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface defining User CRUD operations.
 */
public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(UUID id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    void deleteUser(UUID id);
}

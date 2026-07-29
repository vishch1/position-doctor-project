package com.vishakha.position_doctor_project.domain.position.controller;

import com.vishakha.position_doctor_project.common.dto.ApiResponse;
import com.vishakha.position_doctor_project.domain.position.dto.CreatePositionRequest;
import com.vishakha.position_doctor_project.domain.position.dto.PositionResponse;
import com.vishakha.position_doctor_project.domain.position.dto.UpdatePositionRequest;
import com.vishakha.position_doctor_project.domain.position.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Position CRUD management endpoints.
 */
@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    public ResponseEntity<ApiResponse<PositionResponse>> createPosition(
            @Valid @RequestBody CreatePositionRequest request) {
        PositionResponse response = positionService.createPosition(request);
        return new ResponseEntity<>(ApiResponse.success("Position created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getAllPositions() {
        List<PositionResponse> response = positionService.getAllPositions();
        return ResponseEntity.ok(ApiResponse.success("Positions retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> getPositionById(@PathVariable UUID id) {
        PositionResponse response = positionService.getPositionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getPositionsByPortfolioId(
            @PathVariable UUID portfolioId) {
        List<PositionResponse> response = positionService.getPositionsByPortfolioId(portfolioId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> updatePosition(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePositionRequest request) {
        PositionResponse response = positionService.updatePosition(id, request);
        return ResponseEntity.ok(ApiResponse.success("Position updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePosition(@PathVariable UUID id) {
        positionService.deletePosition(id);
        return ResponseEntity.ok(ApiResponse.success("Position deleted successfully", null));
    }
}

package com.oneness.dine.restaurant.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.restaurant.dto.*;
import com.oneness.dine.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchDto>>> getAllBranches() {
        return ResponseEntity.ok(
                ApiResponse.success(restaurantService.getAllBranches()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDto>> getBranch(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(restaurantService.getBranch(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BranchDto>> createBranch(
            @Valid @RequestBody BranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        restaurantService.createBranch(request),
                        "Branch created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDto>> updateBranch(
            @PathVariable Long id,
            @RequestBody BranchRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        restaurantService.updateBranch(id, request),
                        "Branch updated successfully"));
    }

    @GetMapping("/{id}/settings")
    public ResponseEntity<ApiResponse<BranchSettingsDto>> getBranchSettings(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        restaurantService.getBranchSettings(id)));
    }

    @PutMapping("/{id}/settings")
    public ResponseEntity<ApiResponse<BranchSettingsDto>> updateBranchSettings(
            @PathVariable Long id,
            @RequestBody BranchSettingsDto dto) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        restaurantService.updateBranchSettings(id, dto),
                        "Settings updated successfully"));
    }
}
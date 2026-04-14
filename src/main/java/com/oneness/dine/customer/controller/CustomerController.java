package com.oneness.dine.customer.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.customer.dto.*;
import com.oneness.dine.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CustomerDto>> searchByPhone(
            @RequestParam String phone) {
        return ResponseEntity.ok(
                ApiResponse.success(customerService.searchByPhone(phone)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomer(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(customerService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDto>>> getAllCustomers(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        customerService.getAllByBranch(branchId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        customerService.createCustomer(request),
                        "Customer created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        customerService.updateCustomer(id, request),
                        "Customer updated successfully"));
    }

    @GetMapping("/{id}/loyalty")
    public ResponseEntity<ApiResponse<List<LoyaltyTransactionDto>>> getLoyalty(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        customerService.getLoyaltyHistory(id)));
    }

    @PostMapping("/{id}/loyalty/redeem")
    public ResponseEntity<ApiResponse<LoyaltyTransactionDto>> redeemPoints(
            @PathVariable Long id,
            @Valid @RequestBody RedeemLoyaltyRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        customerService.redeemPoints(id, request),
                        "Points redeemed successfully"));
    }
}

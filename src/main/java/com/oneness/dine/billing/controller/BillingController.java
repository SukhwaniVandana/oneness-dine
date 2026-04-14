package com.oneness.dine.billing.controller;

import com.oneness.dine.billing.dto.*;
import com.oneness.dine.billing.service.BillingService;
import com.oneness.dine.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BillDto>> generateBill(
            @Valid @RequestBody BillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        billingService.generateBill(request),
                        "Bill generated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillDto>> getBill(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(billingService.getBillById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getBills(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long branchId) {
        if (orderId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    billingService.getBillByOrder(orderId)));
        }
        if (branchId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    billingService.getTodaysBills(branchId)));
        }
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @PostMapping("/{id}/print")
    public ResponseEntity<ApiResponse<BillDto>> printBill(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        billingService.printBill(id),
                        "Bill printed successfully"));
    }

    @PatchMapping("/{id}/discount")
    public ResponseEntity<ApiResponse<BillDto>> applyDiscount(
            @PathVariable Long id,
            @Valid @RequestBody ApplyDiscountRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        billingService.applyDiscount(id, request),
                        "Discount applied successfully"));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<BillDto>> settleBill(
            @PathVariable Long id,
            @Valid @RequestBody SettleRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        billingService.settleBill(id, request),
                        "Bill settled successfully"));
    }

    @PostMapping("/{id}/void")
    public ResponseEntity<ApiResponse<BillDto>> voidBill(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Long> body) {
        Long userId = body != null ? body.get("userId") : null;
        return ResponseEntity.ok(
                ApiResponse.success(
                        billingService.voidBill(id, userId),
                        "Bill voided successfully"));
    }
}

package com.oneness.dine.customer.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.customer.dto.CollectCreditRequest;
import com.oneness.dine.customer.dto.CreditLedgerEntryDto;
import com.oneness.dine.customer.dto.DueCustomerDto;
import com.oneness.dine.customer.service.CreditLedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/credit-ledger")
@RequiredArgsConstructor
public class CreditLedgerController {

    private final CreditLedgerService creditLedgerService;

    /**
     * GET /api/credit-ledger/due-customers?branchId=1&from=2024-01-01&to=2024-12-31
     * Returns all customers with outstanding credit dues in the given period.
     */
    @GetMapping("/due-customers")
    public ResponseEntity<ApiResponse<List<DueCustomerDto>>> getDueCustomers(
            @RequestParam Long branchId,
            @RequestParam String from,
            @RequestParam String to) {

        List<DueCustomerDto> customers = creditLedgerService.getDueCustomers(branchId, from, to);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    /**
     * GET /api/credit-ledger/customer/{customerId}?from=2024-01-01&to=2024-12-31
     * Returns the full ledger (entries) for a single customer in the given period.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<CreditLedgerEntryDto>>> getCustomerLedger(
            @PathVariable Long customerId,
            @RequestParam String from,
            @RequestParam String to) {

        List<CreditLedgerEntryDto> entries =
                creditLedgerService.getCustomerLedger(customerId, from, to);
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    /**
     * GET /api/credit-ledger/customer/{customerId}/balance
     * Returns the current outstanding balance for a single customer.
     */
    @GetMapping("/customer/{customerId}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getCustomerBalance(
            @PathVariable Long customerId) {

        BigDecimal balance = creditLedgerService.getOutstandingBalance(customerId);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    /**
     * POST /api/credit-ledger/{branchId}/collect
     * Collects a payment against a customer's outstanding credit balance.
     */
    @PostMapping("/{branchId}/collect")
    public ResponseEntity<ApiResponse<CreditLedgerEntryDto>> collectPayment(
            @PathVariable Long branchId,
            @Valid @RequestBody CollectCreditRequest request) {

        CreditLedgerEntryDto entry = creditLedgerService.collectPayment(branchId, request);
        return ResponseEntity.ok(ApiResponse.success(entry));
    }
}

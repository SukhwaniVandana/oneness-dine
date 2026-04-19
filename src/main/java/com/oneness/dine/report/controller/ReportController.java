package com.oneness.dine.report.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.report.dto.*;
import com.oneness.dine.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ─── DAILY SUMMARY ───────────────────────────────

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailySummaryDto>> getDailySummary(
            @RequestParam Long branchId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(
                ApiResponse.success(
                        reportService.getDailySummary(branchId, date)));
    }

    @PostMapping("/daily/generate")
    public ResponseEntity<ApiResponse<DailySummaryDto>> generateDailySummary(
            @RequestParam Long branchId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(
                ApiResponse.success(
                        reportService.generateDailySummary(branchId, date),
                        "Daily summary generated successfully"));
    }

    // ─── SALES REPORT ────────────────────────────────

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<SalesReportDto>> getSalesReport(
            @RequestParam Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        reportService.getSalesReport(branchId, from, to)));
    }

    // ─── ITEM WISE SALES ─────────────────────────────

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ItemSalesDto>>> getItemSales(
            @RequestParam Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        reportService.getItemWiseSales(branchId, from, to)));
    }

    // ─── PAYMENT REPORT ──────────────────────────────

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentReportDto>> getPaymentReport(
            @RequestParam Long branchId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(
                ApiResponse.success(
                        reportService.getPaymentReport(branchId, date)));
    }

    // ─── CANCELLED ORDERS ────────────────────────────

    @GetMapping("/cancelled")
    public ResponseEntity<ApiResponse<List<CancelledOrderDto>>> getCancelledOrders(
            @RequestParam Long branchId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(
                ApiResponse.success(
                        reportService.getCancelledOrders(branchId, date)));
    }

    // ─── AUDIT LOG ───────────────────────────────────

    @GetMapping("/audit")
    public ResponseEntity<ApiResponse<List<ActivityLogDto>>> getAuditLog(
            @RequestParam Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        reportService.getActivityLog(
                                branchId, from, to, page)));
    }
}

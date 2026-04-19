package com.oneness.dine.payroll.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.payroll.dto.*;
import com.oneness.dine.payroll.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    // ─── EMPLOYEE ────────────────────────────────────

    @GetMapping("/api/v1/employees")
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> getAllEmployees(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        payrollService.getAllEmployees(branchId)));
    }

    @GetMapping("/api/v1/employees/{id}")
    public ResponseEntity<ApiResponse<EmployeeDto>> getEmployee(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(payrollService.getEmployeeById(id)));
    }

    @PostMapping("/api/v1/employees")
    public ResponseEntity<ApiResponse<EmployeeDto>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        payrollService.createEmployee(request),
                        "Employee created successfully"));
    }

    @PutMapping("/api/v1/employees/{id}")
    public ResponseEntity<ApiResponse<EmployeeDto>> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        payrollService.updateEmployee(id, request),
                        "Employee updated successfully"));
    }

    // ─── ATTENDANCE PUNCH ────────────────────────────

    @PostMapping("/api/v1/attendance/punch")
    public ResponseEntity<ApiResponse<AttendancePunchDto>> recordPunch(
            @Valid @RequestBody AttendancePunchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        payrollService.recordPunch(request),
                        "Punch recorded successfully"));
    }

    // ─── ATTENDANCE SUMMARY ──────────────────────────

    @GetMapping("/api/v1/attendance/summary")
    public ResponseEntity<ApiResponse<List<AttendanceSummaryDto>>> getDailySummary(
            @RequestParam Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        payrollService.getDailySummary(branchId, date)));
    }

    @GetMapping("/api/v1/attendance/{employeeId}")
    public ResponseEntity<ApiResponse<List<AttendanceSummaryDto>>> getEmployeeAttendance(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        payrollService.getEmployeeAttendance(
                                employeeId, from, to)));
    }

    @PatchMapping("/api/v1/attendance/summary/{id}/approve")
    public ResponseEntity<ApiResponse<AttendanceSummaryDto>> approveAttendance(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        payrollService.approveAttendance(
                                id, body.get("userId")),
                        "Attendance approved successfully"));
    }

    // ─── PAYROLL PERIOD ──────────────────────────────

    @GetMapping("/api/v1/payroll/periods")
    public ResponseEntity<ApiResponse<List<PayrollPeriodDto>>> getPayrollPeriods(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        payrollService.getPayrollPeriods(branchId)));
    }

    @PostMapping("/api/v1/payroll/periods")
    public ResponseEntity<ApiResponse<PayrollPeriodDto>> createPeriod(
            @Valid @RequestBody PayrollPeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        payrollService.createPayrollPeriod(request),
                        "Payroll period created successfully"));
    }

    @PostMapping("/api/v1/payroll/periods/{id}/calculate")
    public ResponseEntity<ApiResponse<List<PayrollRecordDto>>> calculatePayroll(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        payrollService.calculatePayroll(id),
                        "Payroll calculated successfully"));
    }

    @PostMapping("/api/v1/payroll/periods/{id}/approve")
    public ResponseEntity<ApiResponse<PayrollPeriodDto>> approvePayroll(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        payrollService.approvePayroll(
                                id, body.get("userId")),
                        "Payroll approved successfully"));
    }

    // ─── PAYROLL RECORDS ─────────────────────────────

    @GetMapping("/api/v1/payroll/periods/{id}/records")
    public ResponseEntity<ApiResponse<List<PayrollRecordDto>>> getPayrollRecords(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        payrollService.getPayrollRecords(id)));
    }

    // ─── DEDUCTIONS ──────────────────────────────────

    @PostMapping("/api/v1/payroll/records/{id}/deductions")
    public ResponseEntity<ApiResponse<PayrollDeductionDto>> addDeduction(
            @PathVariable Long id,
            @Valid @RequestBody PayrollDeductionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        payrollService.addDeduction(id, request),
                        "Deduction added successfully"));
    }
}

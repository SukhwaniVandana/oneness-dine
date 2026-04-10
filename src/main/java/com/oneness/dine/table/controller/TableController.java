package com.oneness.dine.table.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.table.dto.*;
import com.oneness.dine.table.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    // ─── SECTIONS ────────────────────────────────────

    @GetMapping("/sections")
    public ResponseEntity<ApiResponse<List<SectionDto>>> getSections(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        tableService.getAllSections(branchId)));
    }

    @PostMapping("/sections")
    public ResponseEntity<ApiResponse<SectionDto>> createSection(
            @Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        tableService.createSection(request),
                        "Section created successfully"));
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<ApiResponse<SectionDto>> updateSection(
            @PathVariable Long id,
            @RequestBody SectionRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        tableService.updateSection(id, request),
                        "Section updated successfully"));
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable Long id) {
        tableService.deleteSection(id);
        return ResponseEntity.ok(
                ApiResponse.success(null,
                        "Section deleted successfully"));
    }

    // ─── TABLES ──────────────────────────────────────

    @GetMapping("/tables")
    public ResponseEntity<ApiResponse<List<TableDto>>> getTables(
            @RequestParam Long branchId,
            @RequestParam(required = false) Long sectionId) {
        if (sectionId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    tableService.getTablesBySection(sectionId)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                tableService.getAllTables(branchId)));
    }

    @GetMapping("/tables/{id}")
    public ResponseEntity<ApiResponse<TableDto>> getTable(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(tableService.getTableById(id)));
    }

    @PostMapping("/tables")
    public ResponseEntity<ApiResponse<TableDto>> createTable(
            @Valid @RequestBody TableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        tableService.createTable(request),
                        "Table created successfully"));
    }

    @PutMapping("/tables/{id}")
    public ResponseEntity<ApiResponse<TableDto>> updateTable(
            @PathVariable Long id,
            @RequestBody TableRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        tableService.updateTable(id, request),
                        "Table updated successfully"));
    }

    @DeleteMapping("/tables/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTable(
            @PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.ok(
                ApiResponse.success(null,
                        "Table deleted successfully"));
    }

    // ─── TABLE LOCKING ───────────────────────────────

    @PostMapping("/tables/{id}/lock")
    public ResponseEntity<ApiResponse<TableDto>> lockTable(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        tableService.lockTable(id,
                                request.get("sessionId")),
                        "Table locked successfully"));
    }

    @DeleteMapping("/tables/{id}/lock")
    public ResponseEntity<ApiResponse<TableDto>> unlockTable(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        tableService.unlockTable(id),
                        "Table unlocked successfully"));
    }

    // ─── TABLE MERGE ─────────────────────────────────

    @PostMapping("/tables/merge")
    public ResponseEntity<ApiResponse<Void>> mergeTables(
            @RequestBody Map<String, Long> request) {
        tableService.mergeTables(
                request.get("primaryTableId"),
                request.get("secondaryTableId"));
        return ResponseEntity.ok(
                ApiResponse.success(null,
                        "Tables merged successfully"));
    }

    @PostMapping("/tables/unmerge")
    public ResponseEntity<ApiResponse<Void>> unmergeTables(
            @RequestBody Map<String, Long> request) {
        tableService.unmergeTables(request.get("tableId"));
        return ResponseEntity.ok(
                ApiResponse.success(null,
                        "Table unmerged successfully"));
    }

    // ─── DASHBOARD ───────────────────────────────────

    @GetMapping("/tables/dashboard")
    public ResponseEntity<ApiResponse<List<TableDashboardDto>>> getDashboard(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        tableService.getDashboard(branchId)));
    }
}
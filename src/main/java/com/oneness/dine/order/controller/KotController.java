package com.oneness.dine.order.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.core.enums.KotDisplayStatus;
import com.oneness.dine.order.dto.*;
import com.oneness.dine.order.service.KotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kot")
@RequiredArgsConstructor
public class KotController {

    private final KotService kotService;

    @PostMapping
    public ResponseEntity<ApiResponse<KotDto>> createKot(
            @Valid @RequestBody KotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        kotService.createKot(request),
                        "KOT created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<KotDto>>> getKots(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status) {

        if (orderId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    kotService.getKotsByOrder(orderId)));
        }
        if (branchId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    kotService.getPendingKots(branchId)));
        }
        return ResponseEntity.ok(
                ApiResponse.success(List.of()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<KotDto>> updateKotStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKotStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        kotService.updateKotStatus(id, request),
                        "KOT status updated"));
    }

    @PatchMapping("/{id}/items/{itemId}/status")
    public ResponseEntity<ApiResponse<KotItemDto>> updateKotItemStatus(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestBody UpdateKotStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        kotService.updateKotItemStatus(
                                id, itemId,
                                request.getDisplayStatus()),
                        "KOT item status updated"));
    }
}
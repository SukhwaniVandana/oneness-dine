package com.oneness.dine.platform.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.core.enums.PlatformType;
import com.oneness.dine.platform.dto.*;
import com.oneness.dine.platform.service.PlatformService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
public class PlatformController {

    private final PlatformService platformService;

    // ─── CONFIG ──────────────────────────────────────

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<List<PlatformConfigDto>>> getAllConfigs(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        platformService.getAllConfigs(branchId)));
    }

    @GetMapping("/config/{id}")
    public ResponseEntity<ApiResponse<PlatformConfigDto>> getConfig(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(platformService.getConfig(id)));
    }

    @PutMapping("/config")
    public ResponseEntity<ApiResponse<PlatformConfigDto>> saveConfig(
            @Valid @RequestBody PlatformConfigRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        platformService.saveConfig(request),
                        "Platform config saved successfully"));
    }

    // ─── PLATFORM ORDERS ─────────────────────────────

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<PlatformOrderDto>>> getOrders(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        platformService.getPlatformOrders(branchId)));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<PlatformOrderDto>> getOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(platformService.getPlatformOrder(id)));
    }

    @PostMapping("/orders/{id}/accept")
    public ResponseEntity<ApiResponse<PlatformOrderDto>> acceptOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        platformService.acceptOrder(id),
                        "Order accepted successfully"));
    }

    @PostMapping("/orders/{id}/reject")
    public ResponseEntity<ApiResponse<PlatformOrderDto>> rejectOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Rejected by restaurant");
        return ResponseEntity.ok(
                ApiResponse.success(
                        platformService.rejectOrder(id, reason),
                        "Order rejected successfully"));
    }

    // ─── WEBHOOKS ────────────────────────────────────
    // These endpoints are called by Swiggy/Zomato servers
    // No JWT auth required — verified by webhook_secret instead

    @PostMapping("/webhook/swiggy")
    public ResponseEntity<ApiResponse<PlatformOrderDto>> swiggyWebhook(
            @RequestParam Long branchId,
            @RequestBody String rawPayload,
            HttpServletRequest request) {
        log.info("Swiggy webhook received for branch: {}", branchId);
        // Extract order ID from raw payload
        // In production: parse JSON and extract external_order_id
        String externalOrderId = "SWG-" + System.currentTimeMillis();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        platformService.handleWebhook(
                                PlatformType.SWIGGY,
                                branchId,
                                rawPayload,
                                externalOrderId),
                        "Webhook processed"));
    }

    @PostMapping("/webhook/zomato")
    public ResponseEntity<ApiResponse<PlatformOrderDto>> zomatoWebhook(
            @RequestParam Long branchId,
            @RequestBody String rawPayload,
            HttpServletRequest request) {
        log.info("Zomato webhook received for branch: {}", branchId);
        String externalOrderId = "ZMT-" + System.currentTimeMillis();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        platformService.handleWebhook(
                                PlatformType.ZOMATO,
                                branchId,
                                rawPayload,
                                externalOrderId),
                        "Webhook processed"));
    }

    // ─── MENU SYNC ───────────────────────────────────

    @PostMapping("/menu-sync")
    public ResponseEntity<ApiResponse<PlatformConfigDto>> syncMenu(
            @RequestParam Long branchId,
            @RequestParam PlatformType platform) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        platformService.syncMenu(branchId, platform),
                        "Menu sync triggered successfully"));
    }
}

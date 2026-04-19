package com.oneness.dine.device.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.device.dto.*;
import com.oneness.dine.device.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // ─── DEVICE REGISTRATION ─────────────────────────

    @PostMapping("/devices/register")
    public ResponseEntity<ApiResponse<DeviceDto>> registerDevice(
            @Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        deviceService.registerDevice(request),
                        "Device registered successfully"));
    }

    @PutMapping("/devices/{terminalId}/heartbeat")
    public ResponseEntity<ApiResponse<DeviceDto>> heartbeat(
            @PathVariable String terminalId,
            @RequestBody HeartbeatRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        deviceService.heartbeat(terminalId, request),
                        "Heartbeat recorded"));
    }

    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<DeviceDto>>> getAllDevices(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        deviceService.getAllDevices(branchId)));
    }

    // ─── KITCHEN DISPLAY SESSION ─────────────────────

    @PostMapping("/devices/kitchen-session")
    public ResponseEntity<ApiResponse<KitchenSessionDto>> startSession(
            @Valid @RequestBody KitchenSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        deviceService.startKitchenSession(request),
                        "Kitchen session started successfully"));
    }

    @DeleteMapping("/devices/kitchen-session/{id}")
    public ResponseEntity<ApiResponse<KitchenSessionDto>> endSession(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        deviceService.endKitchenSession(id),
                        "Kitchen session ended successfully"));
    }

    // ─── PRINTER CONFIG ──────────────────────────────

    @GetMapping("/printers")
    public ResponseEntity<ApiResponse<List<PrinterConfigDto>>> getPrinters(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String terminalId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        deviceService.getPrinters(branchId, terminalId)));
    }

    @PostMapping("/printers")
    public ResponseEntity<ApiResponse<PrinterConfigDto>> savePrinter(
            @Valid @RequestBody PrinterConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        deviceService.savePrinter(request),
                        "Printer config saved successfully"));
    }

    @DeleteMapping("/printers/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePrinter(
            @PathVariable Long id) {
        deviceService.deletePrinter(id);
        return ResponseEntity.ok(
                ApiResponse.success(null,
                        "Printer deleted successfully"));
    }
}

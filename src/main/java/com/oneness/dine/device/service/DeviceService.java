package com.oneness.dine.device.service;

import com.oneness.dine.auth.repository.UserRepository;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.device.dto.*;
import com.oneness.dine.device.entity.DeviceRegistration;
import com.oneness.dine.device.entity.KitchenDisplaySession;
import com.oneness.dine.device.entity.PrinterConfig;
import com.oneness.dine.device.repository.DeviceRegistrationRepository;
import com.oneness.dine.device.repository.KitchenDisplaySessionRepository;
import com.oneness.dine.device.repository.PrinterConfigRepository;
import com.oneness.dine.menu.repository.KitchenStationRepository;
import com.oneness.dine.restaurant.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRegistrationRepository  deviceRepository;
    private final KitchenDisplaySessionRepository sessionRepository;
    private final PrinterConfigRepository       printerRepository;
    private final BranchRepository              branchRepository;
    private final KitchenStationRepository      stationRepository;
    private final UserRepository                userRepository;

    // ─── DEVICE REGISTRATION ─────────────────────────

    @Transactional
    public DeviceDto registerDevice(DeviceRequest request) {
        // If already registered — update and return
        if (deviceRepository.existsByTerminalId(request.getTerminalId())) {
            DeviceRegistration existing = deviceRepository
                    .findByTerminalId(request.getTerminalId()).get();
            existing.setAppVersion(request.getAppVersion());
            existing.setIpAddress(request.getIpAddress());
            existing.setLastSeenAt(LocalDateTime.now());
            existing.setIsActive(true);
            log.info("Device re-registered: {}", request.getTerminalId());
            return toDeviceDto(deviceRepository.save(existing));
        }

        DeviceRegistration device = new DeviceRegistration();
        device.setBranch(branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", request.getBranchId())));
        device.setTerminalId(request.getTerminalId());
        device.setDeviceType(request.getDeviceType());
        device.setDeviceName(request.getDeviceName());
        device.setDeviceModel(request.getDeviceModel());
        device.setOsInfo(request.getOsInfo());
        device.setAppVersion(request.getAppVersion());
        device.setIpAddress(request.getIpAddress());
        device.setIsActive(true);

        DeviceRegistration saved = deviceRepository.save(device);
        log.info("Device registered: {} — {}",
                saved.getTerminalId(), saved.getDeviceType());
        return toDeviceDto(saved);
    }

    @Transactional
    public DeviceDto heartbeat(String terminalId,
                                HeartbeatRequest request) {
        DeviceRegistration device = deviceRepository
                .findByTerminalId(terminalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device", "terminalId", terminalId));

        device.setLastSeenAt(LocalDateTime.now());
        if (request.getAppVersion() != null)
            device.setAppVersion(request.getAppVersion());
        if (request.getIpAddress() != null)
            device.setIpAddress(request.getIpAddress());

        return toDeviceDto(deviceRepository.save(device));
    }

    @Transactional(readOnly = true)
    public List<DeviceDto> getAllDevices(Long branchId) {
        return deviceRepository
                .findByBranchIdAndIsActiveTrueOrderByDeviceNameAsc(branchId)
                .stream().map(this::toDeviceDto)
                .collect(Collectors.toList());
    }

    // ─── KITCHEN DISPLAY SESSION ─────────────────────

    @Transactional
    public KitchenSessionDto startKitchenSession(
            KitchenSessionRequest request) {
        DeviceRegistration device = deviceRepository
                .findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device", "id", request.getDeviceId()));

        // End any existing active session for this device
        sessionRepository.findByDeviceIdAndIsActiveTrue(
                request.getDeviceId()).ifPresent(existing -> {
            existing.setIsActive(false);
            existing.setEndedAt(LocalDateTime.now());
            sessionRepository.save(existing);
        });

        KitchenDisplaySession session = new KitchenDisplaySession();
        session.setBranch(branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", request.getBranchId())));
        session.setDevice(device);
        session.setSessionToken(UUID.randomUUID().toString());
        session.setIsActive(true);

        if (request.getStationId() != null) {
            stationRepository.findById(request.getStationId())
                    .ifPresent(session::setStation);
        }
        if (request.getUserId() != null) {
            userRepository.findById(request.getUserId())
                    .ifPresent(session::setUser);
        }

        KitchenDisplaySession saved = sessionRepository.save(session);
        log.info("Kitchen session started for device: {}",
                device.getTerminalId());
        return toSessionDto(saved);
    }

    @Transactional
    public KitchenSessionDto endKitchenSession(Long sessionId) {
        KitchenDisplaySession session = sessionRepository
                .findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KitchenSession", "id", sessionId));
        session.setIsActive(false);
        session.setEndedAt(LocalDateTime.now());
        log.info("Kitchen session ended: {}", sessionId);
        return toSessionDto(sessionRepository.save(session));
    }

    // ─── PRINTER CONFIG ──────────────────────────────

    @Transactional(readOnly = true)
    public List<PrinterConfigDto> getPrinters(Long branchId,
                                               String terminalId) {
        if (terminalId != null) {
            return printerRepository
                    .findByTerminalIdAndIsActiveTrue(terminalId)
                    .stream().map(this::toPrinterDto)
                    .collect(Collectors.toList());
        }
        return printerRepository
                .findByBranchIdAndIsActiveTrueOrderByPrinterNameAsc(branchId)
                .stream().map(this::toPrinterDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrinterConfigDto savePrinter(PrinterConfigRequest request) {
        // If setting as default — unset existing default of same type
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            printerRepository.findByBranchIdAndPrinterTypeAndIsDefaultTrue(
                    request.getBranchId(), request.getPrinterType())
                    .ifPresent(existing -> {
                        existing.setIsDefault(false);
                        printerRepository.save(existing);
                    });
        }

        PrinterConfig printer = new PrinterConfig();
        printer.setBranch(branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", request.getBranchId())));
        printer.setPrinterName(request.getPrinterName());
        printer.setPrinterType(request.getPrinterType());
        printer.setConnectionType(request.getConnectionType());
        printer.setIpAddress(request.getIpAddress());
        printer.setPort(request.getPort());
        printer.setTerminalId(request.getTerminalId());
        printer.setIsDefault(request.getIsDefault());
        printer.setIsActive(true);

        PrinterConfig saved = printerRepository.save(printer);
        log.info("Printer config saved: {}", saved.getPrinterName());
        return toPrinterDto(saved);
    }

    @Transactional
    public void deletePrinter(Long printerId) {
        PrinterConfig printer = printerRepository.findById(printerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PrinterConfig", "id", printerId));
        printer.setIsActive(false);
        printerRepository.save(printer);
        log.info("Printer soft deleted: {}", printerId);
    }

    // ─── MAPPERS ─────────────────────────────────────

    private DeviceDto toDeviceDto(DeviceRegistration d) {
        return DeviceDto.builder()
                .id(d.getId())
                .branchId(d.getBranch().getId())
                .terminalId(d.getTerminalId())
                .deviceType(d.getDeviceType())
                .deviceName(d.getDeviceName())
                .deviceModel(d.getDeviceModel())
                .osInfo(d.getOsInfo())
                .appVersion(d.getAppVersion())
                .ipAddress(d.getIpAddress())
                .lastSeenAt(d.getLastSeenAt())
                .isActive(d.getIsActive())
                .registeredAt(d.getRegisteredAt())
                .build();
    }

    private KitchenSessionDto toSessionDto(KitchenDisplaySession s) {
        return KitchenSessionDto.builder()
                .id(s.getId())
                .branchId(s.getBranch().getId())
                .deviceId(s.getDevice().getId())
                .terminalId(s.getDevice().getTerminalId())
                .stationId(s.getStation() != null ?
                        s.getStation().getId() : null)
                .stationName(s.getStation() != null ?
                        s.getStation().getName() : null)
                .userId(s.getUser() != null ?
                        s.getUser().getId() : null)
                .sessionToken(s.getSessionToken())
                .startedAt(s.getStartedAt())
                .endedAt(s.getEndedAt())
                .isActive(s.getIsActive())
                .build();
    }

    private PrinterConfigDto toPrinterDto(PrinterConfig p) {
        return PrinterConfigDto.builder()
                .id(p.getId())
                .branchId(p.getBranch().getId())
                .printerName(p.getPrinterName())
                .printerType(p.getPrinterType())
                .connectionType(p.getConnectionType())
                .ipAddress(p.getIpAddress())
                .port(p.getPort())
                .terminalId(p.getTerminalId())
                .isDefault(p.getIsDefault())
                .isActive(p.getIsActive())
                .build();
    }
}

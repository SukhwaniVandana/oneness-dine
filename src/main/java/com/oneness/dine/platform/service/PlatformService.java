package com.oneness.dine.platform.service;

import com.oneness.dine.core.enums.PlatformOrderStatus;
import com.oneness.dine.core.enums.PlatformType;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.platform.dto.*;
import com.oneness.dine.platform.entity.PlatformConfig;
import com.oneness.dine.platform.entity.PlatformOrder;
import com.oneness.dine.platform.repository.PlatformConfigRepository;
import com.oneness.dine.platform.repository.PlatformOrderRepository;
import com.oneness.dine.restaurant.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformService {

    private final PlatformConfigRepository configRepository;
    private final PlatformOrderRepository  orderRepository;
    private final BranchRepository         branchRepository;

    // ─── PLATFORM CONFIG ─────────────────────────────

    @Transactional(readOnly = true)
    public List<PlatformConfigDto> getAllConfigs(Long branchId) {
        return configRepository.findByBranchId(branchId)
                .stream().map(this::toConfigDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlatformConfigDto getConfig(Long configId) {
        return toConfigDto(configRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PlatformConfig", "id", configId)));
    }

    @Transactional
    public PlatformConfigDto saveConfig(PlatformConfigRequest request) {
        // Upsert — update if exists, create if not
        PlatformConfig config = configRepository
                .findByBranchIdAndPlatform(
                        request.getBranchId(), request.getPlatform())
                .orElse(new PlatformConfig());

        config.setBranch(branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", request.getBranchId())));
        config.setPlatform(request.getPlatform());

        if (request.getApiKey()       != null) config.setApiKey(request.getApiKey());
        if (request.getStoreId()      != null) config.setStoreId(request.getStoreId());
        if (request.getWebhookSecret()!= null) config.setWebhookSecret(request.getWebhookSecret());
        if (request.getIsEnabled()    != null) config.setIsEnabled(request.getIsEnabled());
        if (request.getAutoAcceptOrders() != null)
            config.setAutoAcceptOrders(request.getAutoAcceptOrders());
        if (request.getMenuSyncEnabled()  != null)
            config.setMenuSyncEnabled(request.getMenuSyncEnabled());

        PlatformConfig saved = configRepository.save(config);
        log.info("Platform config saved: {} for branch {}",
                request.getPlatform(), request.getBranchId());
        return toConfigDto(saved);
    }

    @Transactional
    public PlatformConfigDto updateMenuSyncTime(Long configId) {
        PlatformConfig config = configRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PlatformConfig", "id", configId));
        config.setLastMenuSyncAt(LocalDateTime.now());
        return toConfigDto(configRepository.save(config));
    }

    // ─── PLATFORM ORDERS ─────────────────────────────

    @Transactional(readOnly = true)
    public List<PlatformOrderDto> getPlatformOrders(Long branchId) {
        return orderRepository
                .findByBranchIdOrderByReceivedAtDesc(branchId)
                .stream().map(this::toOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlatformOrderDto getPlatformOrder(Long id) {
        return toOrderDto(orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PlatformOrder", "id", id)));
    }

    // ─── WEBHOOK HANDLER ─────────────────────────────

    @Transactional
    public PlatformOrderDto handleWebhook(PlatformType platform,
                                           Long branchId,
                                           String rawPayload,
                                           String externalOrderId) {
        // Check if order already received (idempotency)
        if (orderRepository.existsByExternalOrderIdAndPlatform(
                externalOrderId, platform)) {
            log.warn("Duplicate webhook received for order: {}",
                    externalOrderId);
            return toOrderDto(orderRepository
                    .findByExternalOrderIdAndPlatform(
                            externalOrderId, platform).get());
        }

        // Check if platform is enabled for this branch
        PlatformConfig config = configRepository
                .findByBranchIdAndPlatform(branchId, platform)
                .orElseThrow(() -> new BusinessException(
                        "Platform not configured for this branch",
                        "PLATFORM_NOT_CONFIGURED"));

        if (!config.getIsEnabled()) {
            throw new BusinessException(
                    "Platform is disabled for this branch",
                    "PLATFORM_DISABLED");
        }

        // Create platform order record
        PlatformOrder order = new PlatformOrder();
        order.setBranch(branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", branchId)));
        order.setPlatform(platform);
        order.setExternalOrderId(externalOrderId);
        order.setRawPayload(rawPayload);
        order.setOrderStatus(PlatformOrderStatus.RECEIVED);
        order.setInternetAvailable(true);

        // Set acceptance deadline — 3 minutes from received
        order.setAcceptanceDeadlineAt(
                LocalDateTime.now().plusMinutes(3));

        PlatformOrder saved = orderRepository.save(order);
        log.info("Platform order received: {} from {}",
                externalOrderId, platform);

        // Auto accept if configured
        if (Boolean.TRUE.equals(config.getAutoAcceptOrders())) {
            return acceptOrder(saved.getId());
        }

        return toOrderDto(saved);
    }

    // ─── ACCEPT ORDER ────────────────────────────────

    @Transactional
    public PlatformOrderDto acceptOrder(Long platformOrderId) {
        PlatformOrder order = orderRepository.findById(platformOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PlatformOrder", "id", platformOrderId));

        if (order.getOrderStatus() != PlatformOrderStatus.RECEIVED) {
            throw new BusinessException(
                    "Order can only be accepted when in RECEIVED status",
                    "INVALID_ORDER_STATUS");
        }

        order.setOrderStatus(PlatformOrderStatus.ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());

        PlatformOrder saved = orderRepository.save(order);
        log.info("Platform order accepted: {}", order.getExternalOrderId());
        return toOrderDto(saved);
    }

    // ─── REJECT ORDER ────────────────────────────────

    @Transactional
    public PlatformOrderDto rejectOrder(Long platformOrderId,
                                         String reason) {
        PlatformOrder order = orderRepository.findById(platformOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PlatformOrder", "id", platformOrderId));

        if (order.getOrderStatus() != PlatformOrderStatus.RECEIVED) {
            throw new BusinessException(
                    "Order can only be rejected when in RECEIVED status",
                    "INVALID_ORDER_STATUS");
        }

        order.setOrderStatus(PlatformOrderStatus.REJECTED);
        order.setRejectionReason(reason);

        PlatformOrder saved = orderRepository.save(order);
        log.info("Platform order rejected: {} — {}",
                order.getExternalOrderId(), reason);
        return toOrderDto(saved);
    }

    // ─── MENU SYNC ───────────────────────────────────

    @Transactional
    public PlatformConfigDto syncMenu(Long branchId, PlatformType platform) {
        PlatformConfig config = configRepository
                .findByBranchIdAndPlatform(branchId, platform)
                .orElseThrow(() -> new BusinessException(
                        "Platform not configured", "PLATFORM_NOT_CONFIGURED"));

        if (!config.getIsEnabled()) {
            throw new BusinessException(
                    "Platform is disabled", "PLATFORM_DISABLED");
        }

        // Menu sync logic would call platform API here
        // For now we just update the sync timestamp
        config.setLastMenuSyncAt(LocalDateTime.now());
        PlatformConfig saved = configRepository.save(config);

        log.info("Menu synced to {} for branch {}", platform, branchId);
        return toConfigDto(saved);
    }

    // ─── MAPPERS ─────────────────────────────────────

    private PlatformConfigDto toConfigDto(PlatformConfig c) {
        return PlatformConfigDto.builder()
                .id(c.getId())
                .branchId(c.getBranch().getId())
                .platform(c.getPlatform())
                .storeId(c.getStoreId())
                .isEnabled(c.getIsEnabled())
                .autoAcceptOrders(c.getAutoAcceptOrders())
                .menuSyncEnabled(c.getMenuSyncEnabled())
                .lastMenuSyncAt(c.getLastMenuSyncAt())
                .build();
    }

    private PlatformOrderDto toOrderDto(PlatformOrder o) {
        return PlatformOrderDto.builder()
                .id(o.getId())
                .branchId(o.getBranch().getId())
                .platform(o.getPlatform())
                .externalOrderId(o.getExternalOrderId())
                .internalOrderId(o.getInternalOrder() != null ?
                        o.getInternalOrder().getId() : null)
                .internalOrderNumber(o.getInternalOrder() != null ?
                        o.getInternalOrder().getOrderNumber() : null)
                .orderStatus(o.getOrderStatus())
                .acceptanceDeadlineAt(o.getAcceptanceDeadlineAt())
                .rejectionReason(o.getRejectionReason())
                .deliveryPartner(o.getDeliveryPartner())
                .customerAddress(o.getCustomerAddress())
                .platformTotal(o.getPlatformTotal())
                .receivedAt(o.getReceivedAt())
                .acceptedAt(o.getAcceptedAt())
                .internetAvailable(o.getInternetAvailable())
                .build();
    }
}

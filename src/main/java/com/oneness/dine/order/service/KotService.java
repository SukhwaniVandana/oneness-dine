package com.oneness.dine.order.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneness.dine.auth.repository.UserSessionRepository;
import com.oneness.dine.core.enums.KotDisplayStatus;
import com.oneness.dine.core.enums.KotType;
import com.oneness.dine.core.enums.OrderStatus;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.menu.entity.KitchenStation;
import com.oneness.dine.menu.repository.KitchenStationRepository;
import com.oneness.dine.order.dto.KotDto;
import com.oneness.dine.order.dto.KotItemDto;
import com.oneness.dine.order.dto.KotItemRequest;
import com.oneness.dine.order.dto.KotRequest;
import com.oneness.dine.order.dto.UpdateKotStatusRequest;
import com.oneness.dine.order.entity.CustomerOrder;
import com.oneness.dine.order.entity.Kot;
import com.oneness.dine.order.entity.KotItem;
import com.oneness.dine.order.entity.OrderItem;
import com.oneness.dine.order.repository.CustomerOrderRepository;
import com.oneness.dine.order.repository.KotItemRepository;
import com.oneness.dine.order.repository.KotRepository;
import com.oneness.dine.order.repository.OrderItemRepository;
import com.oneness.dine.restaurant.repository.BranchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KotService {

    private final KotRepository            kotRepository;
    private final KotItemRepository        kotItemRepository;
    private final CustomerOrderRepository  orderRepository;
    private final OrderItemRepository      orderItemRepository;
    private final KitchenStationRepository stationRepository;
    private final UserSessionRepository    sessionRepository;
    private final BranchRepository         branchRepository;
    private final KotWebSocketService      kotWebSocketService;   // ← NEW

    // ─── GENERATE KOT NUMBER ─────────────────────────

    private String generateKotNumber(Long branchId) {
        return kotRepository
                .findTopByBranchIdOrderByCreatedAtDesc(branchId)
                .map(k -> {
                    String num = k.getKotNumber()
                            .replace("KOT-", "");
                    try {
                        return String.format("KOT-%04d",
                                Integer.parseInt(num) + 1);
                    } catch (NumberFormatException e) {
                        return "KOT-0001";
                    }
                })
                .orElse("KOT-0001");
    }

    // ─── CREATE KOT ──────────────────────────────────

    @Transactional
    public KotDto createKot(KotRequest request) {
        CustomerOrder order = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", request.getOrderId()));

        if (order.getIsSettled()) {
            throw new BusinessException(
                    "Cannot create KOT for settled order",
                    "ORDER_SETTLED");
        }

        // Build KOT
        Kot kot = new Kot();
        kot.setOrder(order);
        kot.setBranch(order.getBranch());
        kot.setKotNumber(generateKotNumber(order.getBranch().getId()));
        kot.setKotType(KotType.valueOf(
                request.getKotType() != null ?
                request.getKotType() : "NEW"));
        kot.setDisplayStatus(KotDisplayStatus.PENDING);
        kot.setIsPrinted(false);

        // Set table snapshot
        if (order.getTable() != null) {
            kot.setTableNumberSnapshot(
                    order.getTable().getTableNumber());
        }

        // Link kitchen station
        if (request.getStationId() != null) {
            KitchenStation station = stationRepository
                    .findById(request.getStationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "KitchenStation", "id",
                            request.getStationId()));
            kot.setStation(station);
        }

        Kot savedKot = kotRepository.save(kot);

        // Add KOT items
        for (KotItemRequest itemReq : request.getItems()) {
            OrderItem orderItem = orderItemRepository
                    .findById(itemReq.getOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "OrderItem", "id",
                            itemReq.getOrderItemId()));

            KotItem kotItem = new KotItem();
            kotItem.setKot(savedKot);
            kotItem.setOrderItem(orderItem);
            kotItem.setItemNameSnapshot(orderItem.getItemNameSnapshot());
            kotItem.setQuantity(itemReq.getQuantity());
            kotItem.setAction(itemReq.getAction());
            kotItem.setSpecialInstruction(itemReq.getSpecialInstruction());
            kotItem.setKitchenStatus(KotDisplayStatus.PENDING);
            kotItemRepository.save(kotItem);

            // Update kot_quantity on order item
            orderItem.setKotQuantity(orderItem.getKotQuantity() + itemReq.getQuantity());
            orderItem.setItemStatus(OrderStatus.KOT_SENT);
            orderItemRepository.save(orderItem);
        }

        // Update order status
        order.setOrderStatus(OrderStatus.KOT_SENT);
        orderRepository.save(order);

        log.info("KOT created: {} for order: {}",
                savedKot.getKotNumber(), order.getOrderNumber());

        KotDto kotDto = toKotDto(savedKot);
        Long branchId = order.getBranch().getId();

        // ── Broadcast new KOT to kitchen screen ──
        kotWebSocketService.broadcastKotUpdate(branchId, kotDto);
        // ── Broadcast table refresh to captain/tables ──
        kotWebSocketService.broadcastTableUpdate(branchId);

        return kotDto;
    }

    // ─── GET KOTs ────────────────────────────────────

    @Transactional(readOnly = true)
    public List<KotDto> getPendingKots(Long branchId) {
        return kotRepository
                .findByBranchIdAndDisplayStatusInOrderByCreatedAtAsc(
                        branchId,
                        List.of(KotDisplayStatus.PENDING,
                                KotDisplayStatus.PREPARING,
                                KotDisplayStatus.READY,
                                KotDisplayStatus.SERVED))
                        .stream()
                        .filter(kot -> !kot.getOrder().getIsSettled())
                        .map(this::toKotDto)
                        .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KotDto> getKotsByOrder(Long orderId) {
        return kotRepository.findByOrderId(orderId)
                .stream().map(this::toKotDto)
                .collect(Collectors.toList());
    }

    // ─── UPDATE KOT STATUS ───────────────────────────

    @Transactional
    public KotDto updateKotStatus(Long kotId,
                                   UpdateKotStatusRequest request) {
        Kot kot = kotRepository.findById(kotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KOT", "id", kotId));

        KotDisplayStatus newStatus = request.getDisplayStatus();
        kot.setDisplayStatus(newStatus);

        List<KotItem> kotItems = kotItemRepository.findByKotId(kotId);

        if (newStatus == KotDisplayStatus.PREPARING) {
            for (KotItem kotItem : kotItems) {
                kotItem.setKitchenStatus(KotDisplayStatus.PREPARING);
                kotItemRepository.save(kotItem);

                OrderItem orderItem = kotItem.getOrderItem();
                orderItem.setItemStatus(OrderStatus.IN_PROGRESS);
                orderItemRepository.save(orderItem);
            }
            log.info("KOT {} marked PREPARING — {} items updated",
                    kot.getKotNumber(), kotItems.size());
        }

        if (newStatus == KotDisplayStatus.READY) {
            for (KotItem kotItem : kotItems) {
                kotItem.setKitchenStatus(KotDisplayStatus.READY);
                kotItemRepository.save(kotItem);

                OrderItem orderItem = kotItem.getOrderItem();
                orderItem.setItemStatus(OrderStatus.READY);
                orderItemRepository.save(orderItem);
            }

            CustomerOrder order = kot.getOrder();
            order.setOrderStatus(OrderStatus.READY);
            orderRepository.save(order);
            log.info("KOT {} marked READY — order {} and {} items updated",
                    kot.getKotNumber(), order.getOrderNumber(), kotItems.size());
        }

        if (newStatus == KotDisplayStatus.SERVED) {
            for (KotItem kotItem : kotItems) {
                kotItem.setKitchenStatus(KotDisplayStatus.SERVED);
                kotItemRepository.save(kotItem);

                OrderItem orderItem = kotItem.getOrderItem();
                orderItem.setItemStatus(OrderStatus.SERVED);
                orderItemRepository.save(orderItem);
            }

            CustomerOrder order = kot.getOrder();
            order.setOrderStatus(OrderStatus.SERVED);
            orderRepository.save(order);
            log.info("KOT {} marked SERVED — order {} and {} items updated",
                    kot.getKotNumber(), order.getOrderNumber(), kotItems.size());
        }

        KotDto kotDto = toKotDto(kotRepository.save(kot));
        Long branchId = kot.getBranch().getId();

        // ── Broadcast status change to kitchen + captain ──
        kotWebSocketService.broadcastKotUpdate(branchId, kotDto);
        kotWebSocketService.broadcastTableUpdate(branchId);

        return kotDto;
    }

    // ─── UPDATE KOT ITEM STATUS ──────────────────────

    @Transactional
    public KotItemDto updateKotItemStatus(Long kotId,
                                           Long kotItemId,
                                           KotDisplayStatus status) {
        KotItem kotItem = kotItemRepository.findById(kotItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KotItem", "id", kotItemId));
        kotItem.setKitchenStatus(status);

        OrderItem orderItem = kotItem.getOrderItem();
        switch (status) {
            case PREPARING -> orderItem.setItemStatus(OrderStatus.IN_PROGRESS);
            case READY     -> orderItem.setItemStatus(OrderStatus.READY);
            case SERVED    -> orderItem.setItemStatus(OrderStatus.SERVED);
            default        -> { /* PENDING — no change */ }
        }
        orderItemRepository.save(orderItem);

        KotItem saved = kotItemRepository.save(kotItem);
        log.info("KOT item {} status updated to: {} — order item {} updated",
                kotItemId, status, orderItem.getId());

        // ── Broadcast item-level change ──
        Kot kot = kotItem.getKot();
        Long branchId = kot.getBranch().getId();
        kotWebSocketService.broadcastKotUpdate(branchId, toKotDto(kot));
        kotWebSocketService.broadcastTableUpdate(branchId);

        return toKotItemDto(saved);
    }

    // ─── MAPPERS ─────────────────────────────────────

    public KotDto toKotDto(Kot kot) {
        List<KotItemDto> items = kotItemRepository
                .findByKotId(kot.getId())
                .stream().map(this::toKotItemDto)
                .collect(Collectors.toList());

        return KotDto.builder()
                .id(kot.getId())
                .orderId(kot.getOrder().getId())
                .orderNumber(kot.getOrder().getOrderNumber())
                .kotNumber(kot.getKotNumber())
                .kotType(kot.getKotType())
                .tableNumber(kot.getTableNumberSnapshot())
                .stationId(kot.getStation() != null ?
                        kot.getStation().getId() : null)
                .stationName(kot.getStation() != null ?
                        kot.getStation().getName() : null)
                .displayStatus(kot.getDisplayStatus())
                .isPrinted(kot.getIsPrinted())
                .createdAt(kot.getCreatedAt())
                .items(items)
                .build();
    }

    private KotItemDto toKotItemDto(KotItem item) {
        return KotItemDto.builder()
                .id(item.getId())
                .orderItemId(item.getOrderItem().getId())
                .itemName(item.getItemNameSnapshot())
                .quantity(item.getQuantity())
                .action(item.getAction())
                .kitchenStatus(item.getKitchenStatus())
                .specialInstruction(item.getSpecialInstruction())
                .categoryName(item.getOrderItem().getItem() != null ?
                        item.getOrderItem().getItem().getCategory().getName() : null)
                .build();
    }
}

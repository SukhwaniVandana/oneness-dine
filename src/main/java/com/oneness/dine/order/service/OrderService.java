package com.oneness.dine.order.service;

import com.oneness.dine.auth.entity.User;
import com.oneness.dine.auth.entity.UserSession;
import com.oneness.dine.auth.repository.UserRepository;
import com.oneness.dine.auth.repository.UserSessionRepository;
import com.oneness.dine.core.enums.OrderStatus;
import com.oneness.dine.core.enums.TableStatus;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.customer.entity.Customer;
import com.oneness.dine.customer.repository.CustomerRepository;
import com.oneness.dine.menu.entity.AddonItem;
import com.oneness.dine.menu.entity.ItemVariant;
import com.oneness.dine.menu.entity.MenuItem;
import com.oneness.dine.menu.repository.AddonItemRepository;
import com.oneness.dine.menu.repository.ItemVariantRepository;
import com.oneness.dine.menu.repository.MenuItemRepository;
import com.oneness.dine.order.dto.*;
import com.oneness.dine.order.entity.CustomerOrder;
import com.oneness.dine.order.entity.OrderItem;
import com.oneness.dine.order.entity.OrderItemAddon;
import com.oneness.dine.order.repository.CustomerOrderRepository;
import com.oneness.dine.order.repository.OrderItemAddonRepository;
import com.oneness.dine.order.repository.OrderItemRepository;
import com.oneness.dine.restaurant.entity.Branch;
import com.oneness.dine.restaurant.repository.BranchRepository;
import com.oneness.dine.table.entity.RestaurantTable;
import com.oneness.dine.table.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository     orderItemRepository;
    private final OrderItemAddonRepository addonRepository;
    private final BranchRepository        branchRepository;
    private final RestaurantTableRepository tableRepository;
    private final MenuItemRepository      menuItemRepository;
    private final ItemVariantRepository   variantRepository;
    private final AddonItemRepository     addonItemRepository;
    private final UserRepository          userRepository;
    private final UserSessionRepository   sessionRepository;
    private final CustomerRepository      customerRepository;

    // ─── HELPERS ─────────────────────────────────────

    private Branch getBranch(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", branchId));
    }

    private String generateOrderNumber(Long branchId) {
        String date = LocalDate.now().toString().replace("-", "");
        long count = orderRepository.count() + 1;
        return String.format("ORD-%s-%04d", date, count);
    }

    private String generateSessionTag(Long tableId, Long branchId) {
        Integer activeCount = orderRepository
                .countActiveOrdersOnTable(tableId);
        return String.format("T%d-S%03d", tableId,
                (activeCount == null ? 0 : activeCount) + 1);
    }

    // ─── CREATE ORDER ────────────────────────────────

    @Transactional
    public OrderDto createOrder(OrderRequest request) {

        Branch branch = getBranch(request.getBranchId());

        // Build order
        CustomerOrder order = new CustomerOrder();
        order.setBranch(branch);
        order.setOrderNumber(generateOrderNumber(request.getBranchId()));
        order.setOrderType(request.getOrderType());
        order.setPaxCount(request.getPaxCount());
        order.setSpecialInstruction(request.getSpecialInstruction());
        order.setOrderStatus(OrderStatus.DRAFT);
        order.setIsPrinted(false);
        order.setIsSettled(false);
        order.setSessionStartedAt(LocalDateTime.now());

        // Link table if dine-in
        if (request.getTableId() != null) {
            RestaurantTable table = tableRepository
                    .findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Table", "id", request.getTableId()));

            // Generate session tag & index
            String sessionTag = generateSessionTag(
                    request.getTableId(), request.getBranchId());
            Integer activeCount = orderRepository
                    .countActiveOrdersOnTable(request.getTableId());
            Integer sessionIndex = (activeCount == null ? 0 : activeCount) + 1;

            order.setTable(table);
            order.setOrderSessionTag(sessionTag);
            order.setSessionOrderIndex(sessionIndex);

            // Update table status to OCCUPIED
            table.setTableStatus(TableStatus.OCCUPIED);
            tableRepository.save(table);
        } else {
            // Takeaway/Delivery — no table
            order.setOrderSessionTag("TAKEAWAY-" +
                    System.currentTimeMillis());
            order.setSessionOrderIndex(1);
        }

        // Link customer if provided
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository
                    .findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer", "id", request.getCustomerId()));
            order.setCustomer(customer);
        }

        // Link waiter if provided
        if (request.getWaiterUserId() != null) {
            User waiter = userRepository.findById(request.getWaiterUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User", "id", request.getWaiterUserId()));
            order.setWaiterUser(waiter);
        }

        // Link session if provided
        if (request.getSessionId() != null) {
            UserSession session = sessionRepository
                    .findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Session", "id", request.getSessionId()));
            order.setCreatedBySession(session);
        }

        CustomerOrder savedOrder = orderRepository.save(order);

        // Add items
        List<OrderItem> savedItems = new ArrayList<>();
        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem orderItem = buildOrderItem(savedOrder, itemReq);
            savedItems.add(orderItemRepository.save(orderItem));
        }

        // Update order status to SAVED
        savedOrder.setOrderStatus(OrderStatus.SAVED);
        orderRepository.save(savedOrder);

        log.info("Order created: {} for branch {}",
                savedOrder.getOrderNumber(), request.getBranchId());
        return toOrderDto(savedOrder, savedItems);
    }

    // ─── GET ORDER ───────────────────────────────────

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", id));
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        return toOrderDto(order, items);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getActiveOrdersByTable(Long tableId) {
        return orderRepository
                .findByTableIdAndIsSettledFalseAndDeletedAtIsNull(tableId)
                .stream()
                .map(order -> toOrderDto(order,
                        orderItemRepository.findByOrderId(order.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getTodaysOrders(Long branchId) {
        LocalDateTime startOfDay = LocalDateTime.of(
                LocalDate.now(), LocalTime.MIDNIGHT);
        return orderRepository.findTodaysOrders(branchId, startOfDay)
                .stream()
                .map(order -> toOrderDto(order,
                        orderItemRepository.findByOrderId(order.getId())))
                .collect(Collectors.toList());
    }

    // ─── ADD ITEMS ───────────────────────────────────

    @Transactional
    public OrderDto addItemsToOrder(Long orderId,
                                    List<OrderItemRequest> items) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", orderId));

        if (order.getIsSettled()) {
            throw new BusinessException(
                    "Cannot add items to a settled order",
                    "ORDER_SETTLED");
        }

        for (OrderItemRequest itemReq : items) {
            OrderItem orderItem = buildOrderItem(order, itemReq);
            orderItemRepository.save(orderItem);
        }

        log.info("Items added to order: {}", order.getOrderNumber());
        List<OrderItem> allItems = orderItemRepository.findByOrderId(orderId);
        return toOrderDto(order, allItems);
    }

    // ─── UPDATE ORDER STATUS ─────────────────────────

    @Transactional
    public OrderDto updateOrderStatus(Long orderId,
                                      UpdateOrderStatusRequest request) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", orderId));

        order.setOrderStatus(request.getOrderStatus());

        // Handle cancellation
        if (request.getOrderStatus() == OrderStatus.CANCELLED) {
            order.setCancelledAt(LocalDateTime.now());
            order.setCancelReason(request.getCancelReason());

            if (request.getCancelledByUserId() != null) {
                User cancelledBy = userRepository
                        .findById(request.getCancelledByUserId())
                        .orElse(null);
                order.setCancelledByUser(cancelledBy);
            }

            // Free up table if dine-in
            if (order.getTable() != null) {
                updateTableStatusIfNoActiveOrders(order.getTable());
            }
        }

        // Handle save & print
        if (request.getOrderStatus() == OrderStatus.BILLED) {
            order.setIsPrinted(true);
            order.setSaveAndPrintAt(LocalDateTime.now());
        }

        // Handle served
        if (request.getOrderStatus() == OrderStatus.SERVED) {
            order.setServedAt(LocalDateTime.now());
        }

        CustomerOrder saved = orderRepository.save(order);
        log.info("Order {} status updated to: {}",
                order.getOrderNumber(), request.getOrderStatus());

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toOrderDto(saved, items);
    }

    // ─── CANCEL ORDER ITEM ────────────────────────────

    @Transactional
    public OrderDto cancelOrderItem(Long orderId, Long orderItemId,
                                    String reason) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", orderId));

        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OrderItem", "id", orderItemId));

        item.setItemStatus(OrderStatus.CANCELLED);
        orderItemRepository.save(item);

        log.info("Order item {} cancelled from order {}",
                orderItemId, order.getOrderNumber());

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toOrderDto(order, items);
    }

    // ─── SETTLE ORDER ────────────────────────────────

    @Transactional
    public OrderDto settleOrder(Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "id", orderId));

        order.setIsSettled(true);
        order.setOrderStatus(OrderStatus.BILLED);
        CustomerOrder saved = orderRepository.save(order);

        // Update table status if no more active orders
        if (order.getTable() != null) {
            updateTableStatusIfNoActiveOrders(order.getTable());
        }

        log.info("Order {} settled", order.getOrderNumber());
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toOrderDto(saved, items);
    }

    // ─── HELPER — Update table status ────────────────

    private void updateTableStatusIfNoActiveOrders(
            RestaurantTable table) {
        Integer activeOrders = orderRepository
                .countActiveOrdersOnTable(table.getId());
        if (activeOrders == null || activeOrders == 0) {
            table.setTableStatus(TableStatus.AVAILABLE);
            table.setLockedBySession(null);
            table.setLockedAt(null);
            tableRepository.save(table);
            log.info("Table {} set to AVAILABLE", table.getTableNumber());
        }
    }

    // ─── BUILD ORDER ITEM ────────────────────────────

    private OrderItem buildOrderItem(CustomerOrder order,
                                     OrderItemRequest req) {
        MenuItem menuItem = menuItemRepository.findById(req.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MenuItem", "id", req.getItemId()));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setItem(menuItem);
        orderItem.setItemNameSnapshot(menuItem.getName());
        orderItem.setQuantity(req.getQuantity());
        orderItem.setKotQuantity(0);
        orderItem.setItemStatus(OrderStatus.DRAFT);
        orderItem.setIsComplimentary(req.getIsComplimentary());
        orderItem.setSpecialInstruction(req.getSpecialInstruction());

        // Set variant price or base price
        if (req.getVariantId() != null) {
            ItemVariant variant = variantRepository
                    .findById(req.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "ItemVariant", "id", req.getVariantId()));
            orderItem.setVariant(variant);
            orderItem.setUnitPriceSnapshot(variant.getPrice());
        } else {
            orderItem.setUnitPriceSnapshot(menuItem.getBasePrice());
        }

        OrderItem saved = orderItemRepository.save(orderItem);

        // Add addons if any
        if (req.getAddonItemIds() != null &&
                !req.getAddonItemIds().isEmpty()) {
            for (Long addonItemId : req.getAddonItemIds()) {
                AddonItem addonItem = addonItemRepository
                        .findById(addonItemId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "AddonItem", "id", addonItemId));
                OrderItemAddon addon = new OrderItemAddon();
                addon.setOrderItem(saved);
                addon.setAddonItem(addonItem);
                addon.setAddonNameSnapshot(addonItem.getName());
                addon.setPriceSnapshot(addonItem.getPrice());
                addon.setQuantity(1);
                addonRepository.save(addon);
            }
        }

        return saved;
    }

    // ─── MAPPERS ─────────────────────────────────────

    public OrderDto toOrderDto(CustomerOrder order,
                                List<OrderItem> items) {
        List<OrderItemDto> itemDtos = items.stream()
                .map(this::toOrderItemDto)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .branchId(order.getBranch().getId())
                .orderType(order.getOrderType())
                .tableId(order.getTable() != null ?
                        order.getTable().getId() : null)
                .tableNumber(order.getTable() != null ?
                        order.getTable().getTableNumber() : null)
                .orderSessionTag(order.getOrderSessionTag())
                .sessionOrderIndex(order.getSessionOrderIndex())
                .sessionStartedAt(order.getSessionStartedAt())
                .customerId(order.getCustomer() != null ?
                        order.getCustomer().getId() : null)
                .customerName(order.getCustomer() != null ?
                        order.getCustomer().getName() : null)
                .customerPhone(order.getCustomer() != null ?
                        order.getCustomer().getPhone() : null)
                .waiterUserId(order.getWaiterUser() != null ?
                        order.getWaiterUser().getId() : null)
                .waiterName(order.getWaiterUser() != null ?
                        order.getWaiterUser().getFullName() : null)
                .paxCount(order.getPaxCount())
                .orderStatus(order.getOrderStatus())
                .isPrinted(order.getIsPrinted())
                .isSettled(order.getIsSettled())
                .saveAndPrintAt(order.getSaveAndPrintAt())
                .servedAt(order.getServedAt())
                .cancelledAt(order.getCancelledAt())
                .cancelReason(order.getCancelReason())
                .specialInstruction(order.getSpecialInstruction())
                .platformType(order.getPlatformType())
                .createdAt(order.getCreatedAt())
                .items(itemDtos)
                .build();
    }

    private OrderItemDto toOrderItemDto(OrderItem item) {
        List<OrderItemAddonDto> addons = addonRepository
                .findByOrderItemId(item.getId())
                .stream().map(a -> OrderItemAddonDto.builder()
                        .id(a.getId())
                        .addonItemId(a.getAddonItem().getId())
                        .addonName(a.getAddonNameSnapshot())
                        .price(a.getPriceSnapshot())
                        .quantity(a.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderItemDto.builder()
                .id(item.getId())
                .itemId(item.getItem().getId())
                .itemName(item.getItemNameSnapshot())
                .variantId(item.getVariant() != null ?
                        item.getVariant().getId() : null)
                .variantName(item.getVariant() != null ?
                        item.getVariant().getVariantName() : null)
                .unitPrice(item.getUnitPriceSnapshot())
                .quantity(item.getQuantity())
                .kotQuantity(item.getKotQuantity())
                .pendingKotQty(item.getQuantity() - item.getKotQuantity())
                .itemStatus(item.getItemStatus())
                .isComplimentary(item.getIsComplimentary())
                .discountAmount(item.getDiscountAmount())
                .specialInstruction(item.getSpecialInstruction())
                .addons(addons)
                .build();
    }
}
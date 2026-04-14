package com.oneness.dine.order.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.order.dto.*;
import com.oneness.dine.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        orderService.createOrder(request),
                        "Order created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.getOrderById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrders(
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String date) {

        if (tableId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    orderService.getActiveOrdersByTable(tableId)));
        }
        if (branchId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    orderService.getTodaysOrders(branchId)));
        }
        return ResponseEntity.ok(
                ApiResponse.success(List.of()));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<OrderDto>> addItems(
            @PathVariable Long id,
            @Valid @RequestBody List<OrderItemRequest> items) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.addItemsToOrder(id, items),
                        "Items added successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.updateOrderStatus(id, request),
                        "Order status updated"));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<OrderDto>> cancelItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ?
                body.getOrDefault("reason", "") : "";
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.cancelOrderItem(id, itemId, reason),
                        "Item cancelled successfully"));
    }

    @PostMapping("/{id}/settle")
    public ResponseEntity<ApiResponse<OrderDto>> settleOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.settleOrder(id),
                        "Order settled successfully"));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.updateOrderStatus(id, request),
                        "Order cancelled successfully"));
    }
}
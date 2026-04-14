package com.oneness.dine.order.dto;

import com.oneness.dine.core.enums.OrderType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    private Long   tableId;
    private Long   customerId;
    private Long   waiterUserId;
    private Long   sessionId;
    private Integer paxCount = 1;
    private String  specialInstruction;

    @NotNull(message = "At least one item is required")
    private List<OrderItemRequest> items;
}
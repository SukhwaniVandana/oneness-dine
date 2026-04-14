package com.oneness.dine.order.dto;

import com.oneness.dine.core.enums.OrderStatus;
import com.oneness.dine.core.enums.OrderType;
import com.oneness.dine.core.enums.PlatformType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderDto {
    private Long             id;
    private String           orderNumber;
    private Long             branchId;
    private OrderType        orderType;
    private Long             tableId;
    private String           tableNumber;
    private String           orderSessionTag;
    private Integer          sessionOrderIndex;
    private LocalDateTime    sessionStartedAt;
    private Long             customerId;
    private String           customerName;
    private String           customerPhone;
    private Long             waiterUserId;
    private String           waiterName;
    private Integer          paxCount;
    private OrderStatus      orderStatus;
    private Boolean          isPrinted;
    private Boolean          isSettled;
    private LocalDateTime    saveAndPrintAt;
    private LocalDateTime    servedAt;
    private LocalDateTime    cancelledAt;
    private String           cancelReason;
    private String           specialInstruction;
    private PlatformType     platformType;
    private LocalDateTime    createdAt;
    private List<OrderItemDto> items;
}
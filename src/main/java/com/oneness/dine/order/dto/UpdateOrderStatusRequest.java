package com.oneness.dine.order.dto;

import com.oneness.dine.core.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;

    private String cancelReason;
    private Long   cancelledByUserId;
}
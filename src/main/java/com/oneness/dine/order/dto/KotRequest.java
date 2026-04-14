package com.oneness.dine.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KotRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    private Long   stationId;
    private String kotType = "NEW";

    @NotNull(message = "KOT items are required")
    private List<KotItemRequest> items;
}
package com.oneness.dine.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderItemRequest {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    private Long variantId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String      specialInstruction;
    private Boolean     isComplimentary = false;
    private List<Long>  addonItemIds;
}
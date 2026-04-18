package com.oneness.dine.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecipeIngredientRequest {

    @NotNull(message = "Inventory item ID is required")
    private Long inventoryItemId;

    private Long variantId;

    @NotNull(message = "Quantity used is required")
    private BigDecimal quantityUsed;
}

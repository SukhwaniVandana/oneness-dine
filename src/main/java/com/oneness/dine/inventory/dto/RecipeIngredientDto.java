package com.oneness.dine.inventory.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class RecipeIngredientDto {
    private Long       id;
    private Long       itemId;
    private Long       variantId;
    private Long       inventoryItemId;
    private String     inventoryItemName;
    private String     unit;
    private BigDecimal quantityUsed;
}

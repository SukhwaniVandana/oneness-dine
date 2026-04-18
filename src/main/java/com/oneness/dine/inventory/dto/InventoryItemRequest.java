package com.oneness.dine.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InventoryItemRequest {

    @NotBlank(message = "Item name is required")
    private String name;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private BigDecimal minimumStock    = BigDecimal.ZERO;
    private BigDecimal reorderQuantity = BigDecimal.ZERO;
    private BigDecimal costPerUnit     = BigDecimal.ZERO;
    private String     supplierName;
}

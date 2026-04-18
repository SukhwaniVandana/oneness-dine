package com.oneness.dine.inventory.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class InventoryItemDto {
    private Long       id;
    private Long       branchId;
    private String     name;
    private String     unit;
    private BigDecimal currentStock;
    private BigDecimal minimumStock;
    private BigDecimal reorderQuantity;
    private BigDecimal costPerUnit;
    private String     supplierName;
    private Boolean    isActive;
    private Boolean    isLowStock;
}

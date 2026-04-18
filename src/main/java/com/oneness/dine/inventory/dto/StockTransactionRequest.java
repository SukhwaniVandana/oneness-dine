package com.oneness.dine.inventory.dto;

import com.oneness.dine.core.enums.StockTxnType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StockTransactionRequest {

    @NotNull(message = "Inventory item ID is required")
    private Long inventoryItemId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Transaction type is required")
    private StockTxnType txnType;

    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    private BigDecimal unitCost;
    private Long       referenceId;
    private String     referenceType;
    private String     notes;
    private Long       createdByUserId;
}

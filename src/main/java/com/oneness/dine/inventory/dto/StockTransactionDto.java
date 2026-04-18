package com.oneness.dine.inventory.dto;

import com.oneness.dine.core.enums.StockTxnType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class StockTransactionDto {
    private Long          id;
    private Long          inventoryItemId;
    private String        inventoryItemName;
    private Long          branchId;
    private StockTxnType  txnType;
    private BigDecimal    quantity;
    private BigDecimal    unitCost;
    private Long          referenceId;
    private String        referenceType;
    private String        notes;
    private LocalDateTime createdAt;
}

package com.oneness.dine.customer.dto;

import com.oneness.dine.core.enums.CreditTxnType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CreditLedgerEntryDto {
    private Long          id;
    private CreditTxnType txnType;
    private BigDecimal    amount;
    private BigDecimal    balanceAfter;
    private Long          billId;
    private String        billNumber;
    private String        collectedByName;
    private String        remarks;
    private LocalDateTime createdAt;
}

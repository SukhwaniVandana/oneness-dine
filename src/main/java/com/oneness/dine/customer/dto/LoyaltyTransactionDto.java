package com.oneness.dine.customer.dto;

import com.oneness.dine.core.enums.LoyaltyTxnType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class LoyaltyTransactionDto {
    private Long          id;
    private Long          customerId;
    private Long          billId;
    private LoyaltyTxnType txnType;
    private Integer       points;
    private Integer       balanceAfter;
    private String        remarks;
    private LocalDateTime createdAt;
}

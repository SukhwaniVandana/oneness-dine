package com.oneness.dine.customer.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DueCustomerDto {
    private Long       customerId;
    private String     customerName;
    private String     customerPhone;
    private BigDecimal outstandingBalance;
    private Long       billCount;
}

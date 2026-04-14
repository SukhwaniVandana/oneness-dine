package com.oneness.dine.billing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class BillRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    private Long       customerId;
    private Long       createdByUserId;
    private BigDecimal orderDiscount   = BigDecimal.ZERO;
    private String     discountReason;
    private BigDecimal packagingCharge = BigDecimal.ZERO;
    private Boolean    isGstInvoice    = false;
    private List<PaymentRequest> payments;
}

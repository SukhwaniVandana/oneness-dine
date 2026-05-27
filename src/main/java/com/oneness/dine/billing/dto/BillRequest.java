package com.oneness.dine.billing.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

    // [NEW] — discount percentage (0-100), replaces orderDiscount amount
    // On initial bill generation this will be 0
    // On settlement recalculation this will have the cashier-entered %
    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
    private BigDecimal discountPct     = BigDecimal.ZERO;

    private String     discountReason;
    private BigDecimal packagingCharge = BigDecimal.ZERO;
    private Boolean    isGstInvoice    = false;
    private List<PaymentRequest> payments;
}

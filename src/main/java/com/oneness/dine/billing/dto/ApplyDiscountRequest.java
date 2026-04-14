package com.oneness.dine.billing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ApplyDiscountRequest {

    @NotNull(message = "Discount amount is required")
    private BigDecimal discountAmount;

    private String discountReason;
}

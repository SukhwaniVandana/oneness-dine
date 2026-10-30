package com.oneness.dine.billing.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SettleRequest {

    @NotEmpty(message = "At least one payment is required")
    private List<PaymentRequest> payments;

    private Long    createdByUserId;
    private Integer loyaltyPointsToUse = 0;
    private Long    cashierUserId;
}

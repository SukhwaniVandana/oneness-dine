package com.oneness.dine.customer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedeemLoyaltyRequest {

    @NotNull(message = "Points to redeem is required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;

    @NotNull(message = "Bill ID is required")
    private Long billId;

    private String remarks;
}

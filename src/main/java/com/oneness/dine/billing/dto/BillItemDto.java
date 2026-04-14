package com.oneness.dine.billing.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class BillItemDto {
    private Long       orderItemId;
    private String     itemName;
    private String     variantName;
    private Integer    quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    private Boolean    isComplimentary;
}

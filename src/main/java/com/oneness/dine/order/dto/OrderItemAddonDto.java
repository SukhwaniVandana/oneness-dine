package com.oneness.dine.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class OrderItemAddonDto {
    private Long       id;
    private Long       addonItemId;
    private String     addonName;
    private BigDecimal price;
    private Integer    quantity;
}
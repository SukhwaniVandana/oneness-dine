package com.oneness.dine.order.dto;

import com.oneness.dine.core.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderItemDto {
    private Long             id;
    private Long             itemId;
    private String           itemName;
    private Long             variantId;
    private String           variantName;
    private BigDecimal       unitPrice;
    private Integer          quantity;
    private Integer          kotQuantity;
    private Integer          pendingKotQty;
    private OrderStatus      itemStatus;
    private Boolean          isComplimentary;
    private BigDecimal       discountAmount;
    private String           specialInstruction;
    private List<OrderItemAddonDto> addons;
}
package com.oneness.dine.order.dto;

import com.oneness.dine.core.enums.KotDisplayStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class KotItemDto {
    private Long             id;
    private Long             orderItemId;
    private String           itemName;
    private Integer          quantity;
    private String           action;
    private KotDisplayStatus kitchenStatus;
    private String           specialInstruction;
    private String           categoryName;
}
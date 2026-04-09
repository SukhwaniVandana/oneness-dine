package com.oneness.dine.menu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ItemVariantDto {
    private Long       id;
    private Long       itemId;
    private String     variantName;
    private BigDecimal price;
    private Boolean    isDefault;
    private Boolean    isAvailable;
}
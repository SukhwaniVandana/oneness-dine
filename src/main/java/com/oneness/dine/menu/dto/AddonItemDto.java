package com.oneness.dine.menu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AddonItemDto {
    private Long       id;
    private Long       addonGroupId;
    private String     name;
    private BigDecimal price;
    private Boolean    isAvailable;
}
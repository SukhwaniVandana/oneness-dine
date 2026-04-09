package com.oneness.dine.menu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MenuItemImageDto {
    private Long    id;
    private Long    itemId;
    private String  imageUrl;
    private Integer displayOrder;
    private Boolean isPrimary;
}
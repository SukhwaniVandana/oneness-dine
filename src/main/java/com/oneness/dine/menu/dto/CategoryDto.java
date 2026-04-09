package com.oneness.dine.menu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryDto {
    private Long    id;
    private Long    branchId;
    private Long    parentCategoryId;
    private String  parentCategoryName;
    private String  name;
    private Integer displayOrder;
    private String  colorCode;
    private Boolean isActive;
}
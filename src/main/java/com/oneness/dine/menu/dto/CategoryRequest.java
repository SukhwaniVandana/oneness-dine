package com.oneness.dine.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private Long    parentCategoryId;
    private Integer displayOrder = 0;
    private String  colorCode;
}
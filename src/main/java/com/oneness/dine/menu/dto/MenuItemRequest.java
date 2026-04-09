package com.oneness.dine.menu.dto;

import com.oneness.dine.core.enums.FoodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
public class MenuItemRequest {

    @NotBlank(message = "Item name is required")
    private String name;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Base price is required")
    private BigDecimal basePrice;

    private Long      taxGroupId;
    private String    shortName;
    private String    description;
    private FoodType  foodType        = FoodType.VEG;
    private String    itemCode;
    private Boolean   hasVariants     = false;
    private Boolean   hasAddons       = false;
    private Boolean   isRecommended   = false;
    private Boolean   isBestseller    = false;
    private Boolean   isSeasonal      = false;
    private LocalTime availableFromTime;
    private LocalTime availableToTime;
    private Integer   displayOrder    = 0;
}
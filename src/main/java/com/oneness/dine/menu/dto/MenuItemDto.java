package com.oneness.dine.menu.dto;

import com.oneness.dine.core.enums.FoodType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
public class MenuItemDto {
    private Long             id;
    private Long             branchId;
    private Long             categoryId;
    private String           categoryName;
    private Long             taxGroupId;
    private String           taxGroupName;
    private String           name;
    private String           shortName;
    private String           description;
    private FoodType         foodType;
    private BigDecimal       basePrice;
    private String           itemCode;
    private Boolean          hasVariants;
    private Boolean          hasAddons;
    private Boolean          isRecommended;
    private Boolean          isBestseller;
    private Boolean          isSeasonal;
    private LocalTime        availableFromTime;
    private LocalTime        availableToTime;
    private Boolean          isAvailable;
    private Boolean          isActive;
    private Integer          displayOrder;
    private List<MenuItemImageDto> images;
    private String primaryImageUrl;  // convenience field
    private List<ItemVariantDto>  variants;
    private List<AddonGroupDto>   addonGroups;
}
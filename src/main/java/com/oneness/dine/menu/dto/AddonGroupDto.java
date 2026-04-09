package com.oneness.dine.menu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AddonGroupDto {
    private Long              id;
    private Long              branchId;
    private String            name;
    private Boolean           isMandatory;
    private Integer           minSelect;
    private Integer           maxSelect;
    private List<AddonItemDto> items;
}
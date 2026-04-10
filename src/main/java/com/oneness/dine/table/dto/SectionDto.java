package com.oneness.dine.table.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SectionDto {
    private Long    id;
    private Long    branchId;
    private String  name;
    private Integer displayOrder;
    private Boolean isActive;
}
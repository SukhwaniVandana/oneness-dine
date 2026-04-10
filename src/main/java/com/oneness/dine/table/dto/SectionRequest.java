package com.oneness.dine.table.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectionRequest {

    @NotBlank(message = "Section name is required")
    private String name;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private Integer displayOrder = 0;
}
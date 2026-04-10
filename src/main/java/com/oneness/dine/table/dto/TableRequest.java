package com.oneness.dine.table.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableRequest {

    @NotBlank(message = "Table number is required")
    private String tableNumber;

    @NotNull(message = "Section ID is required")
    private Long sectionId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private Integer capacity = 4;
}
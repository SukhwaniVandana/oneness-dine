package com.oneness.dine.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddonGroupRequest {

    @NotBlank(message = "Addon group name is required")
    private String name;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private Boolean isMandatory = false;
    private Integer minSelect   = 0;
    private Integer maxSelect   = 1;
}
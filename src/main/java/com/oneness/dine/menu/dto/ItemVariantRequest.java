package com.oneness.dine.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemVariantRequest {

    @NotBlank(message = "Variant name is required")
    private String variantName;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private Boolean isDefault   = false;
    private Boolean isAvailable = true;
}
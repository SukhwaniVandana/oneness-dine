package com.oneness.dine.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TaxGroupRequest {

    @NotBlank(message = "Tax group name is required")
    private String name;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private BigDecimal cgstPct    = BigDecimal.ZERO;
    private BigDecimal sgstPct    = BigDecimal.ZERO;
    private BigDecimal igstPct    = BigDecimal.ZERO;
    private BigDecimal cessPct    = BigDecimal.ZERO;
    private Boolean    isInclusive = false;
}
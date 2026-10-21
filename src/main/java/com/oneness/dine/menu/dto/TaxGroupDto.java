package com.oneness.dine.menu.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TaxGroupDto {
    private Long       id;
    private Long       branchId;
    private String     name;
    private BigDecimal cgstPct;
    private BigDecimal sgstPct;
    private BigDecimal igstPct;
    private BigDecimal cessPct;
    private Boolean    isInclusive;
    private Boolean    isActive;
    private Boolean    isDefault;
}
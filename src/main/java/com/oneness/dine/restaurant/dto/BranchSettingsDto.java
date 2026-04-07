package com.oneness.dine.restaurant.dto;

import com.oneness.dine.core.enums.KotPrintMode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class BranchSettingsDto {
    private Long    id;
    private Long    branchId;
    private KotPrintMode kotPrintMode;
    private Integer billPrintCopies;
    private BigDecimal serviceChargePct;
    private Boolean roundOffEnabled;
    private Boolean loyaltyEnabled;
    private Boolean tableMergeEnabled;
    private Boolean splitBillEnabled;
    private Boolean multiOrderPerTable;
    private Boolean swiggyEnabled;
    private Boolean zomatoEnabled;
    private Boolean dualScreenMode;
    private Boolean captainAppEnabled;
    private Boolean kitchenDisplayEnabled;
}
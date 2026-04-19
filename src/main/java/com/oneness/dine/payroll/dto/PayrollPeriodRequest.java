package com.oneness.dine.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PayrollPeriodRequest {

    @NotBlank(message = "Period name is required")
    private String periodName;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private Long createdByUserId;
}

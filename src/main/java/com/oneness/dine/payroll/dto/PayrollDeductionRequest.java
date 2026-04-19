package com.oneness.dine.payroll.dto;

import com.oneness.dine.core.enums.DeductionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayrollDeductionRequest {

    @NotNull(message = "Deduction type is required")
    private DeductionType deductionType;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String description;
}

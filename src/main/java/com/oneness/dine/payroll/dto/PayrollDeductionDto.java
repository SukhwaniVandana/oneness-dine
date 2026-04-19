package com.oneness.dine.payroll.dto;

import com.oneness.dine.core.enums.DeductionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PayrollDeductionDto {
    private Long          id;
    private Long          recordId;
    private Long          employeeId;
    private DeductionType deductionType;
    private BigDecimal    amount;
    private String        description;
    private LocalDateTime createdAt;
}

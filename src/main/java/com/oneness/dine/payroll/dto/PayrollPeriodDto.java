package com.oneness.dine.payroll.dto;

import com.oneness.dine.core.enums.PayrollStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PayrollPeriodDto {
    private Long          id;
    private Long          branchId;
    private String        periodName;
    private LocalDate     startDate;
    private LocalDate     endDate;
    private PayrollStatus status;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}

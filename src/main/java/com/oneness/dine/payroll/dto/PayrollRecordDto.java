package com.oneness.dine.payroll.dto;

import com.oneness.dine.core.enums.PaymentMethod;
import com.oneness.dine.core.enums.PaymentStatus;
import com.oneness.dine.core.enums.SalaryType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class PayrollRecordDto {
    private Long          id;
    private Long          periodId;
    private String        periodName;
    private Long          employeeId;
    private String        employeeName;
    private SalaryType    salaryTypeSnapshot;
    private Integer       daysPresent;
    private Integer       daysAbsent;
    private Integer       daysLeave;
    private BigDecimal    totalHoursWorked;
    private BigDecimal    overtimeHours;
    private BigDecimal    grossSalary;
    private BigDecimal    hourlyEarnings;
    private BigDecimal    overtimeEarnings;
    private BigDecimal    totalDeductions;
    private BigDecimal    netSalary;
    private PaymentStatus paymentStatus;
    private LocalDate     paymentDate;
    private PaymentMethod   paymentMode;
    private String        remarks;
    private List<PayrollDeductionDto> deductions;
}

package com.oneness.dine.payroll.dto;

import com.oneness.dine.core.enums.SalaryType;
import com.oneness.dine.core.enums.WeekDay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Salary type is required")
    private SalaryType salaryType;

    private Long       userId;
    private String     employeeCode;
    private String     phone;
    private String     email;
    private String     designation;
    private String     department;
    private BigDecimal monthlySalary          = BigDecimal.ZERO;
    private BigDecimal hourlyRate             = BigDecimal.ZERO;
    private BigDecimal overtimeRateMultiplier = BigDecimal.ONE;
    private WeekDay    weeklyOffDay           = WeekDay.SUN;
    private LocalDate  joiningDate;
    private String     bankAccountNumber;
    private String     bankIfsc;
    private String     bankName;
    private String     panNumber;
    private String     aadharNumber;
    private String     address;
    private String     emergencyContactName;
    private String     emergencyContactPhone;
    private String     punchMachineId;
}

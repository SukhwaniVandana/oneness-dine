package com.oneness.dine.payroll.dto;

import com.oneness.dine.core.enums.SalaryType;
import com.oneness.dine.core.enums.WeekDay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class EmployeeDto {
    private Long       id;
    private Long       branchId;
    private Long       userId;
    private String     employeeCode;
    private String     fullName;
    private String     phone;
    private String     email;
    private String     designation;
    private String     department;
    private SalaryType salaryType;
    private BigDecimal monthlySalary;
    private BigDecimal hourlyRate;
    private BigDecimal overtimeRateMultiplier;
    private WeekDay    weeklyOffDay;
    private LocalDate  joiningDate;
    private LocalDate  relievingDate;
    private String     bankAccountNumber;
    private String     bankIfsc;
    private String     bankName;
    private String     panNumber;
    private String     aadharNumber;
    private String     address;
    private String     emergencyContactName;
    private String     emergencyContactPhone;
    private String     punchMachineId;
    private Boolean    isActive;
}

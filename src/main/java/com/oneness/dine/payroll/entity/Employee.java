package com.oneness.dine.payroll.entity;

import com.oneness.dine.auth.entity.User;
import com.oneness.dine.core.enums.SalaryType;
import com.oneness.dine.core.enums.WeekDay;
import com.oneness.dine.restaurant.entity.Branch;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "employee_code", unique = true, length = 30)
    private String employeeCode;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "department", length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "salary_type", nullable = false)
    private SalaryType salaryType;

    @Column(name = "monthly_salary")
    private BigDecimal monthlySalary = BigDecimal.ZERO;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Column(name = "overtime_rate_multiplier")
    private BigDecimal overtimeRateMultiplier = BigDecimal.ONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "weekly_off_day")
    private WeekDay weeklyOffDay = WeekDay.SUN;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "relieving_date")
    private LocalDate relievingDate;

    @Column(name = "bank_account_number", length = 30)
    private String bankAccountNumber;

    @Column(name = "bank_ifsc", length = 15)
    private String bankIfsc;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "aadhar_number", length = 20)
    private String aadharNumber;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "emergency_contact_name", length = 150)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 15)
    private String emergencyContactPhone;

    @Column(name = "punch_machine_id", length = 50)
    private String punchMachineId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

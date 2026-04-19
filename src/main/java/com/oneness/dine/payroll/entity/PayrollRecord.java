package com.oneness.dine.payroll.entity;

import com.oneness.dine.core.enums.PaymentMethod;
import com.oneness.dine.core.enums.PaymentStatus;
import com.oneness.dine.core.enums.SalaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payroll_record")
public class PayrollRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private PayrollPeriod period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "salary_type_snapshot", nullable = false)
    private SalaryType salaryTypeSnapshot;

    @Column(name = "days_present")
    private Integer daysPresent = 0;

    @Column(name = "days_absent")
    private Integer daysAbsent = 0;

    @Column(name = "days_leave")
    private Integer daysLeave = 0;

    @Column(name = "total_hours_worked")
    private BigDecimal totalHoursWorked = BigDecimal.ZERO;

    @Column(name = "overtime_hours")
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "gross_salary")
    private BigDecimal grossSalary = BigDecimal.ZERO;

    @Column(name = "hourly_earnings")
    private BigDecimal hourlyEarnings = BigDecimal.ZERO;

    @Column(name = "overtime_earnings")
    private BigDecimal overtimeEarnings = BigDecimal.ZERO;

    @Column(name = "total_deductions")
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_salary")
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode")
    private PaymentMethod paymentMode;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

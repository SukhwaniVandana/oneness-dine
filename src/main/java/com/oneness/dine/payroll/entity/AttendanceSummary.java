package com.oneness.dine.payroll.entity;

import com.oneness.dine.auth.entity.User;
import com.oneness.dine.core.enums.AttendanceStatus;
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
@Table(name = "attendance_summary")
public class AttendanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "first_punch_in")
    private LocalDateTime firstPunchIn;

    @Column(name = "last_punch_out")
    private LocalDateTime lastPunchOut;

    @Column(name = "total_hours_worked")
    private BigDecimal totalHoursWorked = BigDecimal.ZERO;

    @Column(name = "break_hours")
    private BigDecimal breakHours = BigDecimal.ZERO;

    @Column(name = "overtime_hours")
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "standard_hours")
    private BigDecimal standardHours = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status")
    private AttendanceStatus attendanceStatus;

    @Column(name = "is_late")
    private Boolean isLate = false;

    @Column(name = "late_minutes")
    private Integer lateMinutes = 0;

    @Column(name = "notes", length = 300)
    private String notes;

    @Column(name = "is_approved")
    private Boolean isApproved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedByUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.oneness.dine.payroll.entity;

import com.oneness.dine.auth.entity.User;
import com.oneness.dine.core.enums.PunchSource;
import com.oneness.dine.core.enums.PunchType;
import com.oneness.dine.restaurant.entity.Branch;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "attendance_punch")
public class AttendancePunch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "punch_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "punch_machine_id", length = 50)
    private String punchMachineId;

    @Column(name = "punch_time", nullable = false)
    private LocalDateTime punchTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "punch_type", nullable = false)
    private PunchType punchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private PunchSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "override_by_user_id")
    private User overrideByUser;

    @Column(name = "override_reason", length = 200)
    private String overrideReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

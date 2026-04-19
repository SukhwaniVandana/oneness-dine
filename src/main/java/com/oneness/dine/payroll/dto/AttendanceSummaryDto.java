package com.oneness.dine.payroll.dto;

import com.oneness.dine.core.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AttendanceSummaryDto {
    private Long             id;
    private Long             employeeId;
    private String           employeeName;
    private Long             branchId;
    private LocalDate        attendanceDate;
    private LocalDateTime    firstPunchIn;
    private LocalDateTime    lastPunchOut;
    private BigDecimal       totalHoursWorked;
    private BigDecimal       breakHours;
    private BigDecimal       overtimeHours;
    private BigDecimal       standardHours;
    private AttendanceStatus attendanceStatus;
    private Boolean          isLate;
    private Integer          lateMinutes;
    private String           notes;
    private Boolean          isApproved;
}

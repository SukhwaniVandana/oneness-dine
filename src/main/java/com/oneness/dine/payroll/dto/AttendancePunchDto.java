package com.oneness.dine.payroll.dto;

import com.oneness.dine.core.enums.PunchSource;
import com.oneness.dine.core.enums.PunchType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AttendancePunchDto {
    private Long          id;
    private Long          employeeId;
    private String        employeeName;
    private Long          branchId;
    private String        punchMachineId;
    private LocalDateTime punchTime;
    private PunchType     punchType;
    private PunchSource   source;
    private String        overrideReason;
    private LocalDateTime createdAt;
}

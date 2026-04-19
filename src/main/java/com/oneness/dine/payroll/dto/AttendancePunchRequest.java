package com.oneness.dine.payroll.dto;

import com.oneness.dine.core.enums.PunchSource;
import com.oneness.dine.core.enums.PunchType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AttendancePunchRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Punch time is required")
    private LocalDateTime punchTime;

    @NotNull(message = "Punch type is required")
    private PunchType punchType;

    private PunchSource source         = PunchSource.MANUAL;
    private String      punchMachineId;
    private Long        overrideByUserId;
    private String      overrideReason;
}

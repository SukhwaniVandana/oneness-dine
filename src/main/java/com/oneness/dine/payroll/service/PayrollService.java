package com.oneness.dine.payroll.service;

import com.oneness.dine.auth.repository.UserRepository;
import com.oneness.dine.core.enums.AttendanceStatus;
import com.oneness.dine.core.enums.PayrollStatus;
import com.oneness.dine.core.enums.PunchSource;
import com.oneness.dine.core.enums.SalaryType;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.payroll.dto.*;
import com.oneness.dine.payroll.entity.*;
import com.oneness.dine.payroll.repository.*;
import com.oneness.dine.restaurant.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollService {

    private final EmployeeRepository          employeeRepository;
    private final AttendancePunchRepository   punchRepository;
    private final AttendanceSummaryRepository summaryRepository;
    private final PayrollPeriodRepository     periodRepository;
    private final PayrollRecordRepository     recordRepository;
    private final PayrollDeductionRepository  deductionRepository;
    private final BranchRepository            branchRepository;
    private final UserRepository              userRepository;

    // ─── EMPLOYEE ────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees(Long branchId) {
        return employeeRepository
                .findByBranchIdAndIsActiveTrueOrderByFullNameAsc(branchId)
                .stream().map(this::toEmployeeDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        return toEmployeeDto(employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee", "id", id)));
    }

    @Transactional
    public EmployeeDto createEmployee(EmployeeRequest request) {
        if (request.getEmployeeCode() != null &&
                employeeRepository.existsByEmployeeCode(
                        request.getEmployeeCode())) {
            throw new BusinessException(
                    "Employee code already exists",
                    "EMPLOYEE_CODE_EXISTS");
        }

        Employee emp = new Employee();
        emp.setBranch(branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", request.getBranchId())));
        emp.setFullName(request.getFullName());
        emp.setEmployeeCode(request.getEmployeeCode());
        emp.setPhone(request.getPhone());
        emp.setEmail(request.getEmail());
        emp.setDesignation(request.getDesignation());
        emp.setDepartment(request.getDepartment());
        emp.setSalaryType(request.getSalaryType());
        emp.setMonthlySalary(request.getMonthlySalary());
        emp.setHourlyRate(request.getHourlyRate());
        emp.setOvertimeRateMultiplier(request.getOvertimeRateMultiplier());
        emp.setWeeklyOffDay(request.getWeeklyOffDay());
        emp.setJoiningDate(request.getJoiningDate());
        emp.setBankAccountNumber(request.getBankAccountNumber());
        emp.setBankIfsc(request.getBankIfsc());
        emp.setBankName(request.getBankName());
        emp.setPanNumber(request.getPanNumber());
        emp.setAadharNumber(request.getAadharNumber());
        emp.setAddress(request.getAddress());
        emp.setEmergencyContactName(request.getEmergencyContactName());
        emp.setEmergencyContactPhone(request.getEmergencyContactPhone());
        emp.setPunchMachineId(request.getPunchMachineId());
        emp.setIsActive(true);

        if (request.getUserId() != null) {
            userRepository.findById(request.getUserId())
                    .ifPresent(emp::setUser);
        }

        Employee saved = employeeRepository.save(emp);
        log.info("Employee created: {}", saved.getFullName());
        return toEmployeeDto(saved);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeRequest request) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee", "id", id));

        if (request.getFullName()              != null) emp.setFullName(request.getFullName());
        if (request.getPhone()                 != null) emp.setPhone(request.getPhone());
        if (request.getEmail()                 != null) emp.setEmail(request.getEmail());
        if (request.getDesignation()           != null) emp.setDesignation(request.getDesignation());
        if (request.getDepartment()            != null) emp.setDepartment(request.getDepartment());
        if (request.getMonthlySalary()         != null) emp.setMonthlySalary(request.getMonthlySalary());
        if (request.getHourlyRate()            != null) emp.setHourlyRate(request.getHourlyRate());
        if (request.getWeeklyOffDay()          != null) emp.setWeeklyOffDay(request.getWeeklyOffDay());
        if (request.getBankAccountNumber()     != null) emp.setBankAccountNumber(request.getBankAccountNumber());
        if (request.getBankIfsc()              != null) emp.setBankIfsc(request.getBankIfsc());
        if (request.getBankName()              != null) emp.setBankName(request.getBankName());
        if (request.getAddress()               != null) emp.setAddress(request.getAddress());
        if (request.getPunchMachineId()        != null) emp.setPunchMachineId(request.getPunchMachineId());

        return toEmployeeDto(employeeRepository.save(emp));
    }

    // ─── ATTENDANCE PUNCH ────────────────────────────

    @Transactional
    public AttendancePunchDto recordPunch(AttendancePunchRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee", "id", request.getEmployeeId()));

        AttendancePunch punch = new AttendancePunch();
        punch.setBranch(employee.getBranch());
        punch.setEmployee(employee);
        punch.setPunchTime(request.getPunchTime());
        punch.setPunchType(request.getPunchType());
        punch.setSource(request.getSource() != null ?
                request.getSource() : PunchSource.MANUAL);
        punch.setPunchMachineId(request.getPunchMachineId());
        punch.setOverrideReason(request.getOverrideReason());

        if (request.getOverrideByUserId() != null) {
            userRepository.findById(request.getOverrideByUserId())
                    .ifPresent(punch::setOverrideByUser);
        }

        AttendancePunch saved = punchRepository.save(punch);
        log.info("Punch recorded: {} {} for employee {}",
                request.getPunchType(), request.getPunchTime(),
                employee.getFullName());
        return toPunchDto(saved);
    }

    // ─── ATTENDANCE SUMMARY ──────────────────────────

    @Transactional(readOnly = true)
    public List<AttendanceSummaryDto> getDailySummary(
            Long branchId, LocalDate date) {
        return summaryRepository
                .findByBranchIdAndAttendanceDateOrderByEmployeeIdAsc(
                        branchId, date)
                .stream().map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceSummaryDto> getEmployeeAttendance(
            Long employeeId, LocalDate from, LocalDate to) {
        return summaryRepository
                .findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                        employeeId, from, to)
                .stream().map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttendanceSummaryDto approveAttendance(Long summaryId, Long userId) {
        AttendanceSummary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AttendanceSummary", "id", summaryId));
        summary.setIsApproved(true);
        userRepository.findById(userId).ifPresent(summary::setApprovedByUser);
        return toSummaryDto(summaryRepository.save(summary));
    }

    // ─── PAYROLL PERIOD ──────────────────────────────

    @Transactional(readOnly = true)
    public List<PayrollPeriodDto> getPayrollPeriods(Long branchId) {
        return periodRepository.findByBranchIdOrderByStartDateDesc(branchId)
                .stream().map(this::toPeriodDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PayrollPeriodDto createPayrollPeriod(PayrollPeriodRequest request) {
        PayrollPeriod period = new PayrollPeriod();
        period.setBranch(branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", request.getBranchId())));
        period.setPeriodName(request.getPeriodName());
        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        period.setStatus(PayrollStatus.DRAFT);

        if (request.getCreatedByUserId() != null) {
            userRepository.findById(request.getCreatedByUserId())
                    .ifPresent(period::setCreatedByUser);
        }

        PayrollPeriod saved = periodRepository.save(period);
        log.info("Payroll period created: {}", saved.getPeriodName());
        return toPeriodDto(saved);
    }

    // ─── PAYROLL CALCULATION ─────────────────────────

    @Transactional
    public List<PayrollRecordDto> calculatePayroll(Long periodId) {
        PayrollPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PayrollPeriod", "id", periodId));

        if (period.getStatus() != PayrollStatus.DRAFT) {
            throw new BusinessException(
                    "Payroll already calculated for this period",
                    "PAYROLL_ALREADY_PROCESSED");
        }

        // Performance: fetch all employees once
        List<Employee> employees = employeeRepository
                .findByBranchIdAndIsActiveTrueOrderByFullNameAsc(
                        period.getBranch().getId());

        List<PayrollRecord> records = employees.stream().map(emp -> {
            // Performance: use aggregate queries — no N+1
            BigDecimal totalHours = summaryRepository.sumHoursWorked(
                    emp.getId(), period.getStartDate(), period.getEndDate());
            Integer daysPresent = summaryRepository.countDaysPresent(
                    emp.getId(), period.getStartDate(), period.getEndDate());

            if (totalHours == null) totalHours = BigDecimal.ZERO;
            if (daysPresent == null) daysPresent = 0;

            PayrollRecord record = new PayrollRecord();
            record.setPeriod(period);
            record.setEmployee(emp);
            record.setSalaryTypeSnapshot(emp.getSalaryType());
            record.setDaysPresent(daysPresent);
            record.setTotalHoursWorked(totalHours);

            // Calculate earnings based on salary type
            BigDecimal gross = BigDecimal.ZERO;
            if (emp.getSalaryType() == SalaryType.MONTHLY) {
                // Pro-rate monthly salary based on days present
                // Assuming 26 working days per month
                gross = emp.getMonthlySalary()
                        .multiply(BigDecimal.valueOf(daysPresent))
                        .divide(BigDecimal.valueOf(26), 2, RoundingMode.HALF_UP);
            } else {
                // Hourly rate calculation
                gross = emp.getHourlyRate()
                        .multiply(totalHours)
                        .setScale(2, RoundingMode.HALF_UP);
                record.setHourlyEarnings(gross);
            }

            record.setGrossSalary(gross);
            record.setTotalDeductions(BigDecimal.ZERO);
            record.setNetSalary(gross);

            return recordRepository.save(record);
        }).collect(Collectors.toList());

        // Update period status
        period.setStatus(PayrollStatus.PROCESSING);
        periodRepository.save(period);

        log.info("Payroll calculated for period: {} — {} records",
                period.getPeriodName(), records.size());

        return records.stream().map(r -> toRecordDto(r,
                deductionRepository.findByRecordId(r.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public PayrollPeriodDto approvePayroll(Long periodId, Long userId) {
        PayrollPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PayrollPeriod", "id", periodId));

        if (period.getStatus() != PayrollStatus.PROCESSING) {
            throw new BusinessException(
                    "Payroll must be in PROCESSING state to approve",
                    "INVALID_PAYROLL_STATE");
        }

        period.setStatus(PayrollStatus.APPROVED);
        period.setApprovedAt(java.time.LocalDateTime.now());
        userRepository.findById(userId)
                .ifPresent(period::setApprovedByUser);

        log.info("Payroll approved: {}", period.getPeriodName());
        return toPeriodDto(periodRepository.save(period));
    }

    // ─── PAYROLL RECORDS ─────────────────────────────

    @Transactional(readOnly = true)
    public List<PayrollRecordDto> getPayrollRecords(Long periodId) {
        return recordRepository.findByPeriodId(periodId)
                .stream()
                .map(r -> toRecordDto(r,
                        deductionRepository.findByRecordId(r.getId())))
                .collect(Collectors.toList());
    }

    // ─── DEDUCTIONS ──────────────────────────────────

    @Transactional
    public PayrollDeductionDto addDeduction(Long recordId,
                                             PayrollDeductionRequest request) {
        PayrollRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PayrollRecord", "id", recordId));

        PayrollDeduction deduction = new PayrollDeduction();
        deduction.setRecord(record);
        deduction.setEmployee(record.getEmployee());
        deduction.setDeductionType(request.getDeductionType());
        deduction.setAmount(request.getAmount());
        deduction.setDescription(request.getDescription());

        PayrollDeduction saved = deductionRepository.save(deduction);

        // Update record total deductions and net salary
        BigDecimal totalDeductions = deductionRepository
                .sumDeductionsByRecord(recordId);
        record.setTotalDeductions(totalDeductions);
        record.setNetSalary(record.getGrossSalary().subtract(totalDeductions));
        recordRepository.save(record);

        log.info("Deduction added to record {}: {} {}",
                recordId, request.getDeductionType(), request.getAmount());
        return toDeductionDto(saved);
    }

    // ─── MAPPERS ─────────────────────────────────────

    private EmployeeDto toEmployeeDto(Employee e) {
        return EmployeeDto.builder()
                .id(e.getId())
                .branchId(e.getBranch().getId())
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .employeeCode(e.getEmployeeCode())
                .fullName(e.getFullName())
                .phone(e.getPhone())
                .email(e.getEmail())
                .designation(e.getDesignation())
                .department(e.getDepartment())
                .salaryType(e.getSalaryType())
                .monthlySalary(e.getMonthlySalary())
                .hourlyRate(e.getHourlyRate())
                .overtimeRateMultiplier(e.getOvertimeRateMultiplier())
                .weeklyOffDay(e.getWeeklyOffDay())
                .joiningDate(e.getJoiningDate())
                .relievingDate(e.getRelievingDate())
                .bankAccountNumber(e.getBankAccountNumber())
                .bankIfsc(e.getBankIfsc())
                .bankName(e.getBankName())
                .panNumber(e.getPanNumber())
                .aadharNumber(e.getAadharNumber())
                .address(e.getAddress())
                .emergencyContactName(e.getEmergencyContactName())
                .emergencyContactPhone(e.getEmergencyContactPhone())
                .punchMachineId(e.getPunchMachineId())
                .isActive(e.getIsActive())
                .build();
    }

    private AttendancePunchDto toPunchDto(AttendancePunch p) {
        return AttendancePunchDto.builder()
                .id(p.getId())
                .employeeId(p.getEmployee().getId())
                .employeeName(p.getEmployee().getFullName())
                .branchId(p.getBranch().getId())
                .punchMachineId(p.getPunchMachineId())
                .punchTime(p.getPunchTime())
                .punchType(p.getPunchType())
                .source(p.getSource())
                .overrideReason(p.getOverrideReason())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private AttendanceSummaryDto toSummaryDto(AttendanceSummary s) {
        return AttendanceSummaryDto.builder()
                .id(s.getId())
                .employeeId(s.getEmployee().getId())
                .employeeName(s.getEmployee().getFullName())
                .branchId(s.getBranch().getId())
                .attendanceDate(s.getAttendanceDate())
                .firstPunchIn(s.getFirstPunchIn())
                .lastPunchOut(s.getLastPunchOut())
                .totalHoursWorked(s.getTotalHoursWorked())
                .breakHours(s.getBreakHours())
                .overtimeHours(s.getOvertimeHours())
                .standardHours(s.getStandardHours())
                .attendanceStatus(s.getAttendanceStatus())
                .isLate(s.getIsLate())
                .lateMinutes(s.getLateMinutes())
                .notes(s.getNotes())
                .isApproved(s.getIsApproved())
                .build();
    }

    private PayrollPeriodDto toPeriodDto(PayrollPeriod p) {
        return PayrollPeriodDto.builder()
                .id(p.getId())
                .branchId(p.getBranch().getId())
                .periodName(p.getPeriodName())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .status(p.getStatus())
                .approvedAt(p.getApprovedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private PayrollRecordDto toRecordDto(PayrollRecord r,
                                          List<PayrollDeduction> deductions) {
        return PayrollRecordDto.builder()
                .id(r.getId())
                .periodId(r.getPeriod().getId())
                .periodName(r.getPeriod().getPeriodName())
                .employeeId(r.getEmployee().getId())
                .employeeName(r.getEmployee().getFullName())
                .salaryTypeSnapshot(r.getSalaryTypeSnapshot())
                .daysPresent(r.getDaysPresent())
                .daysAbsent(r.getDaysAbsent())
                .daysLeave(r.getDaysLeave())
                .totalHoursWorked(r.getTotalHoursWorked())
                .overtimeHours(r.getOvertimeHours())
                .grossSalary(r.getGrossSalary())
                .hourlyEarnings(r.getHourlyEarnings())
                .overtimeEarnings(r.getOvertimeEarnings())
                .totalDeductions(r.getTotalDeductions())
                .netSalary(r.getNetSalary())
                .paymentStatus(r.getPaymentStatus())
                .paymentDate(r.getPaymentDate())
                .paymentMode(r.getPaymentMode())
                .remarks(r.getRemarks())
                .deductions(deductions.stream()
                        .map(this::toDeductionDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private PayrollDeductionDto toDeductionDto(PayrollDeduction d) {
        return PayrollDeductionDto.builder()
                .id(d.getId())
                .recordId(d.getRecord().getId())
                .employeeId(d.getEmployee().getId())
                .deductionType(d.getDeductionType())
                .amount(d.getAmount())
                .description(d.getDescription())
                .createdAt(d.getCreatedAt())
                .build();
    }
}

package com.oneness.dine.payroll.repository;

import com.oneness.dine.payroll.entity.AttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSummaryRepository
        extends JpaRepository<AttendanceSummary, Long> {

    List<AttendanceSummary> findByBranchIdAndAttendanceDateOrderByEmployeeIdAsc(
            Long branchId, LocalDate date);

    List<AttendanceSummary> findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
            Long employeeId, LocalDate from, LocalDate to);

    Optional<AttendanceSummary> findByEmployeeIdAndAttendanceDate(
            Long employeeId, LocalDate date);

    // Performance: aggregate for payroll calculation
    @Query("SELECT SUM(a.totalHoursWorked) FROM AttendanceSummary a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :from AND :to")
    java.math.BigDecimal sumHoursWorked(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT COUNT(a) FROM AttendanceSummary a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :from AND :to " +
           "AND a.attendanceStatus = 'PRESENT'")
    Integer countDaysPresent(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}

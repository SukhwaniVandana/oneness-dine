package com.oneness.dine.payroll.repository;

import com.oneness.dine.payroll.entity.AttendancePunch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendancePunchRepository
        extends JpaRepository<AttendancePunch, Long> {

    List<AttendancePunch> findByEmployeeIdAndPunchTimeBetweenOrderByPunchTimeAsc(
            Long employeeId,
            LocalDateTime start,
            LocalDateTime end);

    List<AttendancePunch> findByBranchIdAndPunchTimeBetweenOrderByPunchTimeAsc(
            Long branchId,
            LocalDateTime start,
            LocalDateTime end);
}

package com.oneness.dine.payroll.repository;

import com.oneness.dine.payroll.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRecordRepository
        extends JpaRepository<PayrollRecord, Long> {
    List<PayrollRecord> findByPeriodId(Long periodId);
    Optional<PayrollRecord> findByPeriodIdAndEmployeeId(
            Long periodId, Long employeeId);
    List<PayrollRecord> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}

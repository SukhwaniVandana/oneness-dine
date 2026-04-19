package com.oneness.dine.payroll.repository;

import com.oneness.dine.core.enums.PayrollStatus;
import com.oneness.dine.payroll.entity.PayrollPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollPeriodRepository
        extends JpaRepository<PayrollPeriod, Long> {
    List<PayrollPeriod> findByBranchIdOrderByStartDateDesc(Long branchId);
    List<PayrollPeriod> findByBranchIdAndStatusOrderByStartDateDesc(
            Long branchId, PayrollStatus status);
}

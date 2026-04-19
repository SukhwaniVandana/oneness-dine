package com.oneness.dine.payroll.repository;

import com.oneness.dine.payroll.entity.PayrollDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PayrollDeductionRepository
        extends JpaRepository<PayrollDeduction, Long> {
    List<PayrollDeduction> findByRecordId(Long recordId);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM PayrollDeduction d " +
           "WHERE d.record.id = :recordId")
    BigDecimal sumDeductionsByRecord(@Param("recordId") Long recordId);
}

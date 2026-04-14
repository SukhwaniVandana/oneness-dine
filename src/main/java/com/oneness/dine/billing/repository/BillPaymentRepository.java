package com.oneness.dine.billing.repository;

import com.oneness.dine.billing.entity.BillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {

    List<BillPayment> findByBillId(Long billId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM BillPayment p " +
           "WHERE p.bill.id = :billId AND p.paymentStatus = 'SUCCESS'")
    BigDecimal getTotalPaidForBill(@Param("billId") Long billId);
}

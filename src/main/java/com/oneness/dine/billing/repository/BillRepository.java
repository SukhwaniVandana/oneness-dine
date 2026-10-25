package com.oneness.dine.billing.repository;

import com.oneness.dine.billing.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByOrderId(Long orderId);
    Optional<Bill> findByBillNumber(String billNumber);
    Optional<Bill> findByOrderIdAndDeletedAtIsNull(Long orderId);
    boolean existsByBillNumber(String billNumber);

    @Query("SELECT b FROM Bill b WHERE b.branch.id = :branchId " +
           "AND b.createdAt >= :startOfDay AND b.deletedAt IS NULL " +
           "ORDER BY b.createdAt DESC")
    List<Bill> findTodaysBills(@Param("branchId") Long branchId,
                               @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b " +
           "WHERE b.branch.id = :branchId AND b.billStatus = 'PAID' " +
           "AND b.createdAt >= :startOfDay")
    BigDecimal getTotalRevenueToday(@Param("branchId") Long branchId,
                                    @Param("startOfDay") LocalDateTime startOfDay);

    List<Bill> findByBranchIdAndCreatedAtBetween(Long branchId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}

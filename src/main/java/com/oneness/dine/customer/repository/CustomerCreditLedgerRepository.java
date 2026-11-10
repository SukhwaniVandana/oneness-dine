package com.oneness.dine.customer.repository;

import com.oneness.dine.customer.entity.CustomerCreditLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerCreditLedgerRepository
        extends JpaRepository<CustomerCreditLedger, Long> {

    /** All ledger entries for a customer, newest first */
    List<CustomerCreditLedger> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /** Ledger entries for a customer within a date range, newest first */
    @Query("""
        SELECT l FROM CustomerCreditLedger l
        WHERE l.customer.id = :customerId
          AND l.createdAt BETWEEN :from AND :to
        ORDER BY l.createdAt DESC
        """)
    List<CustomerCreditLedger> findByCustomerIdAndDateRange(
            @Param("customerId") Long customerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /** Current outstanding balance for a customer (last entry's balanceAfter) */
    @Query(value = """
        SELECT COALESCE(l.balance_after, 0)
        FROM customer_credit_ledger l
        WHERE l.customer_id = :customerId
        ORDER BY l.created_at DESC
        LIMIT 1
        """, nativeQuery = true)
    BigDecimal findCurrentBalance(@Param("customerId") Long customerId);

    /**
     * All customers in a branch who have any ledger activity.
     * currentBalance = balanceAfter of their latest entry (DEBIT or CREDIT).
     *   > 0  = owes money,  < 0 = has advance,  0 = fully settled.
     * Ordered: dues first (DESC), then advances (most negative last).
     */
    @Query(value = """
        SELECT
            c.customer_id   AS customerId,
            c.name          AS customerName,
            c.phone         AS customerPhone,
            latest.balance  AS currentBalance,
            debits.billCount AS billCount
        FROM customer c
        JOIN (
            SELECT l1.customer_id,
                   l1.balance_after AS balance
            FROM customer_credit_ledger l1
            INNER JOIN (
                SELECT customer_id, MAX(created_at) AS max_created
                FROM customer_credit_ledger
                WHERE branch_id = :branchId
                GROUP BY customer_id
            ) l1max ON l1.customer_id = l1max.customer_id
                    AND l1.created_at  = l1max.max_created
        ) latest ON latest.customer_id = c.customer_id
        JOIN (
            SELECT customer_id,
                   COUNT(DISTINCT bill_id) AS billCount
            FROM customer_credit_ledger
            WHERE branch_id = :branchId
              AND txn_type = 'DEBIT'
            GROUP BY customer_id
        ) debits ON debits.customer_id = c.customer_id
        ORDER BY latest.balance DESC
        """, nativeQuery = true)
    List<Object[]> findDueCustomerSummary(
            @Param("branchId") Long branchId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /**
     * All customers in a branch who have EVER had credit dues
     * (balance may be zero now — used for full ledger view).
     */
    @Query("""
        SELECT DISTINCT l.customer.id FROM CustomerCreditLedger l
        WHERE l.branch.id = :branchId
        """)
    List<Long> findCustomerIdsWithLedger(@Param("branchId") Long branchId);

    /** DEBIT entries whose linked bill is still unpaid (CREDIT_DUE or PARTIAL_CREDIT), oldest first */
    @Query("""
        SELECT l FROM CustomerCreditLedger l
        WHERE l.customer.id = :customerId
          AND l.txnType = com.oneness.dine.core.enums.CreditTxnType.DEBIT
          AND l.bill IS NOT NULL
          AND l.bill.billStatus IN (
              com.oneness.dine.core.enums.BillStatus.CREDIT_DUE,
              com.oneness.dine.core.enums.BillStatus.PARTIAL_CREDIT
          )
        ORDER BY l.createdAt ASC
        """)
    List<CustomerCreditLedger> findUnpaidDebitEntriesOldestFirst(
            @Param("customerId") Long customerId);
}

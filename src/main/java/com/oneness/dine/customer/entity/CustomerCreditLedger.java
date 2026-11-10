package com.oneness.dine.customer.entity;

import com.oneness.dine.auth.entity.User;
import com.oneness.dine.billing.entity.Bill;
import com.oneness.dine.core.enums.CreditTxnType;
import com.oneness.dine.restaurant.entity.Branch;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "customer_credit_ledger")
public class CustomerCreditLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    /**
     * Nullable — DEBIT entries are linked to a bill; CREDIT (collection) entries are not.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false)
    private CreditTxnType txnType;

    /** Always positive. For DEBIT: amount the customer owes. For CREDIT: amount collected. */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** Running account balance AFTER this entry. Positive = customer still owes money. */
    @Column(name = "balance_after", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    /**
     * For DEBIT entries only: how much credit remains uncollected on this bill.
     * Set to {@code amount} when the entry is created; decremented as collections are applied
     * in {@code adjustBillStatuses()}. Null for CREDIT (collection) entries.
     */
    @Column(name = "remaining_credit", precision = 10, scale = 2)
    private BigDecimal remainingCredit;

    /** User who collected the payment (populated only for CREDIT entries). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collected_by_user_id")
    private User collectedBy;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

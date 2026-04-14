package com.oneness.dine.billing.entity;

import com.oneness.dine.auth.entity.User;
import com.oneness.dine.core.enums.BillStatus;
import com.oneness.dine.customer.entity.Customer;
import com.oneness.dine.order.entity.CustomerOrder;
import com.oneness.dine.restaurant.entity.Branch;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bill")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "bill_number", unique = true, length = 30)
    private String billNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "item_discount")
    private BigDecimal itemDiscount = BigDecimal.ZERO;

    @Column(name = "order_discount")
    private BigDecimal orderDiscount = BigDecimal.ZERO;

    @Column(name = "discount_reason", length = 200)
    private String discountReason;

    @Column(name = "cgst_amount")
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_amount")
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(name = "igst_amount")
    private BigDecimal igstAmount = BigDecimal.ZERO;

    @Column(name = "cess_amount")
    private BigDecimal cessAmount = BigDecimal.ZERO;

    @Column(name = "service_charge")
    private BigDecimal serviceCharge = BigDecimal.ZERO;

    @Column(name = "packaging_charge")
    private BigDecimal packagingCharge = BigDecimal.ZERO;

    @Column(name = "round_off")
    private BigDecimal roundOff = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "change_returned")
    private BigDecimal changeReturned = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "bill_status")
    private BillStatus billStatus = BillStatus.DRAFT;

    @Column(name = "is_gst_invoice")
    private Boolean isGstInvoice = false;

    @Column(name = "loyalty_points_used")
    private Integer loyaltyPointsUsed = 0;

    @Column(name = "loyalty_points_earned")
    private Integer loyaltyPointsEarned = 0;

    @Column(name = "printed_at")
    private LocalDateTime printedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by_user_id")
    private User deletedByUser;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

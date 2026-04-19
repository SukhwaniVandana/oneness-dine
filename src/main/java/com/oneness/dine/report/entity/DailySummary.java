package com.oneness.dine.report.entity;

import com.oneness.dine.restaurant.entity.Branch;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "daily_summary")
public class DailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "dine_in_orders")
    private Integer dineInOrders = 0;

    @Column(name = "takeaway_orders")
    private Integer takeawayOrders = 0;

    @Column(name = "online_orders")
    private Integer onlineOrders = 0;

    @Column(name = "total_revenue")
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "cash_collected")
    private BigDecimal cashCollected = BigDecimal.ZERO;

    @Column(name = "card_collected")
    private BigDecimal cardCollected = BigDecimal.ZERO;

    @Column(name = "upi_collected")
    private BigDecimal upiCollected = BigDecimal.ZERO;

    @Column(name = "total_discount")
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(name = "total_tax")
    private BigDecimal totalTax = BigDecimal.ZERO;

    @Column(name = "cancelled_orders")
    private Integer cancelledOrders = 0;

    @Column(name = "new_customers")
    private Integer newCustomers = 0;

    @Column(name = "avg_order_value")
    private BigDecimal avgOrderValue = BigDecimal.ZERO;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        generatedAt = LocalDateTime.now();
    }
}

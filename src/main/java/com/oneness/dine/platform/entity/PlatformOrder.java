package com.oneness.dine.platform.entity;

import com.oneness.dine.core.enums.PlatformOrderStatus;
import com.oneness.dine.core.enums.PlatformType;
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
@Table(name = "platform_order")
public class PlatformOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "platform_order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private PlatformType platform;

    @Column(name = "external_order_id", length = 100)
    private String externalOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_order_id")
    private CustomerOrder internalOrder;

    @Column(name = "raw_payload", columnDefinition = "json")
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private PlatformOrderStatus orderStatus = PlatformOrderStatus.RECEIVED;

    @Column(name = "acceptance_deadline_at")
    private LocalDateTime acceptanceDeadlineAt;

    @Column(name = "rejection_reason", length = 300)
    private String rejectionReason;

    @Column(name = "delivery_partner", length = 100)
    private String deliveryPartner;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "platform_total")
    private BigDecimal platformTotal = BigDecimal.ZERO;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Column(name = "internet_available")
    private Boolean internetAvailable = true;

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}

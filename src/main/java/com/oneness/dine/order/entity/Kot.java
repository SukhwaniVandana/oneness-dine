package com.oneness.dine.order.entity;

import com.oneness.dine.auth.entity.UserSession;
import com.oneness.dine.core.enums.KotDisplayStatus;
import com.oneness.dine.core.enums.KotType;
import com.oneness.dine.menu.entity.KitchenStation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "kot")
public class Kot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private com.oneness.dine.restaurant.entity.Branch branch;

    @Column(name = "kot_number", nullable = false, length = 30)
    private String kotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "kot_type")
    private KotType kotType = KotType.NEW;

    @Column(name = "table_number_snapshot", length = 20)
    private String tableNumberSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private KitchenStation station;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_status")
    private KotDisplayStatus displayStatus = KotDisplayStatus.PENDING;

    @Column(name = "printed_at")
    private LocalDateTime printedAt;

    @Column(name = "is_printed")
    private Boolean isPrinted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_session_id")
    private UserSession createdBySession;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
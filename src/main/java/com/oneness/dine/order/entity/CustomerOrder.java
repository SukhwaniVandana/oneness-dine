package com.oneness.dine.order.entity;

import com.oneness.dine.auth.entity.User;
import com.oneness.dine.auth.entity.UserSession;
import com.oneness.dine.core.entity.BaseEntity;
import com.oneness.dine.core.enums.OrderStatus;
import com.oneness.dine.core.enums.OrderType;
import com.oneness.dine.core.enums.PlatformType;
import com.oneness.dine.customer.entity.Customer;
import com.oneness.dine.restaurant.entity.Branch;
import com.oneness.dine.table.entity.RestaurantTable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "customer_order")
@AttributeOverrides({
    @AttributeOverride(name = "id",
        column = @Column(name = "order_id")),
    @AttributeOverride(name = "createdAt",
        column = @Column(name = "created_at")),
    @AttributeOverride(name = "updatedAt",
        column = @Column(name = "updated_at"))
})
public class CustomerOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType = OrderType.DINE_IN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private RestaurantTable table;

    @Column(name = "order_session_tag", nullable = false, length = 50)
    private String orderSessionTag;

    @Column(name = "session_order_index")
    private Integer sessionOrderIndex = 1;

    @Column(name = "session_started_at")
    private LocalDateTime sessionStartedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiter_user_id")
    private User waiterUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_user_id")
    private User cashierUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_session_id")
    private UserSession createdBySession;

    @Column(name = "pax_count")
    private Integer paxCount = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus = OrderStatus.DRAFT;

    @Column(name = "is_printed")
    private Boolean isPrinted = false;

    @Column(name = "is_settled")
    private Boolean isSettled = false;

    @Column(name = "save_and_print_at")
    private LocalDateTime saveAndPrintAt;

    @Column(name = "served_at")
    private LocalDateTime servedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 300)
    private String cancelReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by_user_id")
    private User cancelledByUser;

    @Column(name = "special_instruction", columnDefinition = "TEXT")
    private String specialInstruction;

    @Column(name = "platform_order_id", length = 100)
    private String platformOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type")
    private PlatformType platformType = PlatformType.NONE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by_user_id")
    private User deletedByUser;
}
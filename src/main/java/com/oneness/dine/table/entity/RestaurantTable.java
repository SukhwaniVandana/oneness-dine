package com.oneness.dine.table.entity;

import com.oneness.dine.auth.entity.UserSession;
import com.oneness.dine.auth.entity.User;
import com.oneness.dine.core.entity.BaseEntity;
import com.oneness.dine.core.enums.TableStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "restaurant_table")
@AttributeOverride(name = "id", column = @Column(name = "table_id"))
public class RestaurantTable extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private com.oneness.dine.restaurant.entity.Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "table_number", nullable = false, length = 20)
    private String tableNumber;

    @Column(name = "capacity")
    private Integer capacity = 4;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_status")
    private TableStatus tableStatus = TableStatus.AVAILABLE;

    @Column(name = "is_merged")
    private Boolean isMerged = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merged_with_table_id")
    private RestaurantTable mergedWithTable;

    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiter_user_id")
    private User waiterUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by_session_id")
    private UserSession lockedBySession;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
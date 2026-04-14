package com.oneness.dine.order.entity;

import com.oneness.dine.core.enums.KotDisplayStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "kot_item")
public class KotItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kot_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kot_id", nullable = false)
    private Kot kot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "item_name_snapshot", nullable = false, length = 200)
    private String itemNameSnapshot;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "action", length = 20)
    private String action = "ADD";

    @Enumerated(EnumType.STRING)
    @Column(name = "kitchen_status")
    private KotDisplayStatus kitchenStatus = KotDisplayStatus.PENDING;

    @Column(name = "special_instruction", columnDefinition = "TEXT")
    private String specialInstruction;
}
package com.oneness.dine.order.entity;

import com.oneness.dine.menu.entity.AddonItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "order_item_addon")
public class OrderItemAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_item_id", nullable = false)
    private AddonItem addonItem;

    @Column(name = "addon_name_snapshot", nullable = false, length = 100)
    private String addonNameSnapshot;

    @Column(name = "price_snapshot", nullable = false)
    private BigDecimal priceSnapshot;

    @Column(name = "quantity")
    private Integer quantity = 1;
}
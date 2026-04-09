package com.oneness.dine.menu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "addon_item")
public class AddonItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "addon_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_group_id", nullable = false)
    private AddonGroup addonGroup;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price")
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "is_available")
    private Boolean isAvailable = true;
}
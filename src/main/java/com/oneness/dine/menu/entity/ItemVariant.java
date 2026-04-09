package com.oneness.dine.menu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "item_variant")
public class ItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem item;

    @Column(name = "variant_name", nullable = false, length = 100)
    private String variantName;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "is_available")
    private Boolean isAvailable = true;
}
package com.oneness.dine.inventory.entity;

import com.oneness.dine.restaurant.entity.Branch;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "inventory_item")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "unit", nullable = false, length = 30)
    private String unit;

    @Column(name = "current_stock")
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "minimum_stock")
    private BigDecimal minimumStock = BigDecimal.ZERO;

    @Column(name = "reorder_quantity")
    private BigDecimal reorderQuantity = BigDecimal.ZERO;

    @Column(name = "cost_per_unit")
    private BigDecimal costPerUnit = BigDecimal.ZERO;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "is_active")
    private Boolean isActive = true;
}

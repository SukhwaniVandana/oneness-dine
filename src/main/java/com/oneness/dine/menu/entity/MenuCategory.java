package com.oneness.dine.menu.entity;

import com.oneness.dine.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "menu_category")
@AttributeOverride(name = "id", column = @Column(name = "category_id"))
public class MenuCategory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private com.oneness.dine.restaurant.entity.Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private MenuCategory parentCategory;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "color_code", length = 10)
    private String colorCode;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
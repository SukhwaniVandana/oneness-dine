package com.oneness.dine.menu.entity;

import com.oneness.dine.core.entity.BaseEntity;
import com.oneness.dine.core.enums.FoodType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "menu_item")
@AttributeOverrides({
        @AttributeOverride(name = "id",        column = @Column(name = "item_id")),
        @AttributeOverride(name = "createdAt", column = @Column(name = "created_at")),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at"))
})
public class MenuItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private com.oneness.dine.restaurant.entity.Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_group_id")
    private TaxGroup taxGroup;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_type")
    private FoodType foodType = FoodType.VEG;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "item_code", unique = true, length = 30)
    private String itemCode;

    @Column(name = "has_variants")
    private Boolean hasVariants = false;

    @Column(name = "has_addons")
    private Boolean hasAddons = false;

    @Column(name = "is_recommended")
    private Boolean isRecommended = false;

    @Column(name = "is_bestseller")
    private Boolean isBestseller = false;

    @Column(name = "is_seasonal")
    private Boolean isSeasonal = false;

    @Column(name = "available_from_time")
    private LocalTime availableFromTime;

    @Column(name = "available_to_time")
    private LocalTime availableToTime;

    @Column(name = "swiggy_item_id", length = 100)
    private String swiggyItemId;

    @Column(name = "zomato_item_id", length = 100)
    private String zomatoItemId;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

}
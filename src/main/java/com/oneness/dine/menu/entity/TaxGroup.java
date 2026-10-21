package com.oneness.dine.menu.entity;

import com.oneness.dine.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "tax_group")
@AttributeOverride(name = "id", column = @Column(name = "tax_group_id"))
public class TaxGroup extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private com.oneness.dine.restaurant.entity.Branch branch;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "cgst_pct")
    private BigDecimal cgstPct = BigDecimal.ZERO;

    @Column(name = "sgst_pct")
    private BigDecimal sgstPct = BigDecimal.ZERO;

    @Column(name = "igst_pct")
    private BigDecimal igstPct = BigDecimal.ZERO;

    @Column(name = "cess_pct")
    private BigDecimal cessPct = BigDecimal.ZERO;

    @Column(name = "is_inclusive")
    private Boolean isInclusive = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_default")
    private Boolean isDefault = true;
}
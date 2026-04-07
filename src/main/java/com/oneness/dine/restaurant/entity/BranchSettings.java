package com.oneness.dine.restaurant.entity;

import com.oneness.dine.core.enums.KotPrintMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "branch_settings")
public class BranchSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false, unique = true)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "kot_print_mode")
    private KotPrintMode kotPrintMode = KotPrintMode.AUTO;

    @Column(name = "bill_print_copies")
    private Integer billPrintCopies = 1;

    @Column(name = "service_charge_pct")
    private BigDecimal serviceChargePct = BigDecimal.ZERO;

    @Column(name = "round_off_enabled")
    private Boolean roundOffEnabled = true;

    @Column(name = "loyalty_enabled")
    private Boolean loyaltyEnabled = false;

    @Column(name = "table_merge_enabled")
    private Boolean tableMergeEnabled = true;

    @Column(name = "split_bill_enabled")
    private Boolean splitBillEnabled = true;

    @Column(name = "multi_order_per_table")
    private Boolean multiOrderPerTable = true;

    @Column(name = "swiggy_enabled")
    private Boolean swiggyEnabled = false;

    @Column(name = "zomato_enabled")
    private Boolean zomatoEnabled = false;

    @Column(name = "dual_screen_mode")
    private Boolean dualScreenMode = false;

    @Column(name = "captain_app_enabled")
    private Boolean captainAppEnabled = false;

    @Column(name = "kitchen_display_enabled")
    private Boolean kitchenDisplayEnabled = false;
}
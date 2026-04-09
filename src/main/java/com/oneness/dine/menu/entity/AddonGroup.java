package com.oneness.dine.menu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "addon_group")
public class AddonGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "addon_group_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private com.oneness.dine.restaurant.entity.Branch branch;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_mandatory")
    private Boolean isMandatory = false;

    @Column(name = "min_select")
    private Integer minSelect = 0;

    @Column(name = "max_select")
    private Integer maxSelect = 1;
}
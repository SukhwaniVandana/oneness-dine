package com.oneness.dine.menu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "item_addon_group_map")
public class ItemAddonGroupMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_group_id", nullable = false)
    private AddonGroup addonGroup;
}
package com.oneness.dine.menu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "kitchen_station")
public class KitchenStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "station_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private com.oneness.dine.restaurant.entity.Branch branch;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "printer_ip", length = 20)
    private String printerIp;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
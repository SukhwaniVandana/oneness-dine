package com.oneness.dine.restaurant.entity;

import com.oneness.dine.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "branch")
@AttributeOverride(name = "id", column = @Column(name = "branch_id"))
public class Branch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "branch_name", nullable = false, length = 150)
    private String branchName;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "is_24_hours")
    private Boolean is24Hours = false;

    @Column(name = "supports_dine_in")
    private Boolean supportsDineIn = true;

    @Column(name = "supports_takeaway")
    private Boolean supportsTakeaway = true;

    @Column(name = "supports_delivery")
    private Boolean supportsDelivery = false;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
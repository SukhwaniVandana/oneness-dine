package com.oneness.dine.restaurant.entity;

import com.oneness.dine.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "restaurant")
@AttributeOverride(name = "id", column = @Column(name = "restaurant_id"))
public class Restaurant extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "legal_name", length = 200)
    private String legalName;

    @Column(name = "gstin", unique = true, length = 20)
    private String gstin;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "fssai_number", length = 30)
    private String fssaiNumber;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "currency", length = 5)
    private String currency = "INR";

    @Column(name = "timezone", length = 50)
    private String timezone = "Asia/Kolkata";

    @Column(name = "is_active")
    private Boolean isActive = true;
}
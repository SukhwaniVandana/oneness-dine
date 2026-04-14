package com.oneness.dine.customer.entity;

import com.oneness.dine.core.entity.BaseEntity;
import com.oneness.dine.restaurant.entity.Branch;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "customer")
@AttributeOverride(name = "id", column = @Column(name = "customer_id"))
public class Customer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "phone", nullable = false, unique = true, length = 15)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "anniversary_date")
    private LocalDate anniversaryDate;

    @Column(name = "total_visits")
    private Integer totalVisits = 0;

    @Column(name = "total_spent")
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "loyalty_points_balance")
    private Integer loyaltyPointsBalance = 0;

    @Column(name = "customer_tag", length = 50)
    private String customerTag;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
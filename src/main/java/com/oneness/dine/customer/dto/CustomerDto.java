package com.oneness.dine.customer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class CustomerDto {
    private Long       id;
    private Long       branchId;
    private String     name;
    private String     phone;
    private String     email;
    private String     gstin;
    private String     companyName;
    private String     address;
    private LocalDate  dateOfBirth;
    private LocalDate  anniversaryDate;
    private Integer    totalVisits;
    private BigDecimal totalSpent;
    private Integer    loyaltyPointsBalance;
    private String     customerTag;
    private Boolean    isActive;
}

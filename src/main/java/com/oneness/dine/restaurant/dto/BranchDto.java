package com.oneness.dine.restaurant.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Builder
public class BranchDto {
    private Long      id;
    private String    branchName;
    private String    address;
    private String    city;
    private String    state;
    private String    pincode;
    private String    phone;
    private String    gstin;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean   is24Hours;
    private Boolean   supportsDineIn;
    private Boolean   supportsTakeaway;
    private Boolean   supportsDelivery;
    private Boolean   isActive;
    private Long      restaurantId;
}
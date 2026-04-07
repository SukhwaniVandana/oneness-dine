package com.oneness.dine.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class BranchRequest {

    @NotBlank(message = "Branch name is required")
    private String branchName;

    @NotBlank(message = "Address is required")
    private String address;

    private String    city;
    private String    state;
    private String    pincode;
    private String    phone;
    private String    gstin;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean   is24Hours       = false;
    private Boolean   supportsDineIn  = true;
    private Boolean   supportsTakeaway = true;
    private Boolean   supportsDelivery = false;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
}
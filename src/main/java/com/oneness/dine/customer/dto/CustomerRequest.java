package com.oneness.dine.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private String    email;
    private String    gstin;
    private String    companyName;
    private String    address;
    private LocalDate dateOfBirth;
    private LocalDate anniversaryDate;
    private String    customerTag;
}

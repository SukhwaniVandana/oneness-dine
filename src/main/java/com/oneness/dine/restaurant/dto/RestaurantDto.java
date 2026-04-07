package com.oneness.dine.restaurant.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RestaurantDto {
    private Long   id;
    private String name;
    private String legalName;
    private String gstin;
    private String panNumber;
    private String fssaiNumber;
    private String email;
    private String phone;
    private String logoUrl;
    private String currency;
    private String timezone;
    private Boolean isActive;
}
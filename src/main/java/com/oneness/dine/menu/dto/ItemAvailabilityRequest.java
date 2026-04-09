package com.oneness.dine.menu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemAvailabilityRequest {

    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;
}
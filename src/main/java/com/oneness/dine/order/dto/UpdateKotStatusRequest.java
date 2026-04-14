package com.oneness.dine.order.dto;

import com.oneness.dine.core.enums.KotDisplayStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateKotStatusRequest {

    @NotNull(message = "Display status is required")
    private KotDisplayStatus displayStatus;
}
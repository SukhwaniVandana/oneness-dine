package com.oneness.dine.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KitchenSessionRequest {

    @NotNull(message = "Device ID is required")
    private Long deviceId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private Long stationId;
    private Long userId;
}

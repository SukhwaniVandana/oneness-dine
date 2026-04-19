package com.oneness.dine.platform.dto;

import com.oneness.dine.core.enums.PlatformType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformConfigRequest {

    @NotNull(message = "Platform is required")
    private PlatformType platform;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private String  apiKey;
    private String  storeId;
    private String  webhookSecret;
    private Boolean isEnabled          = false;
    private Boolean autoAcceptOrders   = false;
    private Boolean menuSyncEnabled    = false;
}

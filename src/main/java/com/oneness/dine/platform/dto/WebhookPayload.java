package com.oneness.dine.platform.dto;

import com.oneness.dine.core.enums.PlatformType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebhookPayload {
    private PlatformType platform;
    private Long         branchId;
    private String       rawPayload;
    private String       externalOrderId;
    private String       customerAddress;
    private String       deliveryPartner;
    private Double       platformTotal;
}

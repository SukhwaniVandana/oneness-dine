package com.oneness.dine.platform.dto;

import com.oneness.dine.core.enums.PlatformOrderStatus;
import com.oneness.dine.core.enums.PlatformType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PlatformOrderDto {
    private Long                id;
    private Long                branchId;
    private PlatformType        platform;
    private String              externalOrderId;
    private Long                internalOrderId;
    private String              internalOrderNumber;
    private PlatformOrderStatus orderStatus;
    private LocalDateTime       acceptanceDeadlineAt;
    private String              rejectionReason;
    private String              deliveryPartner;
    private String              customerAddress;
    private BigDecimal          platformTotal;
    private LocalDateTime       receivedAt;
    private LocalDateTime       acceptedAt;
    private Boolean             internetAvailable;
}

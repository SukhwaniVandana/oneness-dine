package com.oneness.dine.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CancelledOrderDto {
    private Long          orderId;
    private String        orderNumber;
    private String        tableNumber;
    private String        cancelReason;
    private LocalDateTime cancelledAt;
    private String        cancelledBy;
}

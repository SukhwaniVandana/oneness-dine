package com.oneness.dine.billing.dto;

import com.oneness.dine.core.enums.PaymentMethod;
import com.oneness.dine.core.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PaymentDto {
    private Long          id;
    private Long          billId;
    private PaymentMethod paymentMethod;
    private BigDecimal    amount;
    private String        referenceNumber;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidAt;
    private String        remarks;
}

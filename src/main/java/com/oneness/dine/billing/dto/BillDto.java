package com.oneness.dine.billing.dto;

import com.oneness.dine.core.enums.BillStatus;
import com.oneness.dine.restaurant.entity.Branch;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class BillDto {
    private Long          id;
    private String        billNumber;
    private Long          orderId;
    private String        orderNumber;
    private Long          branchId;
    private String        branchName;
    private String        branchAddress;
    private String        branchGstin;
    private Long          customerId;
    private String        customerName;
    private String        customerPhone;
    private BigDecimal    subtotal;
    private BigDecimal    itemDiscount;
    private BigDecimal    orderDiscount;   // total discount amount (derived from discountPct)
    private BigDecimal    discountPct;     // [NEW] — the % applied
    private String        discountReason;
    private BigDecimal    cgstAmount;
    private BigDecimal    sgstAmount;
    private BigDecimal    igstAmount;
    private BigDecimal    cessAmount;
    private BigDecimal    serviceCharge;
    private BigDecimal    packagingCharge;
    private BigDecimal    roundOff;
    private BigDecimal    totalAmount;
    private BigDecimal    paidAmount;
    private BigDecimal    changeReturned;
    private BillStatus    billStatus;
    private Boolean       isGstInvoice;
    private Integer       loyaltyPointsUsed;
    private Integer       loyaltyPointsEarned;
    private LocalDateTime printedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private List<BillItemDto>  items;
    private List<PaymentDto>   payments;
}

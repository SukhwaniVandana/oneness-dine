package com.oneness.dine.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class PaymentReportDto {
    private Long       branchId;
    private LocalDate  fromDate;
    private LocalDate  toDate;
    private BigDecimal cashTotal;
    private BigDecimal cardTotal;
    private BigDecimal upiTotal;
    private BigDecimal walletTotal;
    private BigDecimal otherTotal;
    private BigDecimal grandTotal;
    private Integer    totalTransactions;
}

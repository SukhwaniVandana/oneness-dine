package com.oneness.dine.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DailySummaryDto {
    private Long          id;
    private Long          branchId;
    private LocalDate     summaryDate;
    private Integer       totalOrders;
    private Integer       dineInOrders;
    private Integer       takeawayOrders;
    private Integer       onlineOrders;
    private BigDecimal    totalRevenue;
    private BigDecimal    cashCollected;
    private BigDecimal    cardCollected;
    private BigDecimal    upiCollected;
    private BigDecimal    totalDiscount;
    private BigDecimal    totalTax;
    private Integer       cancelledOrders;
    private Integer       newCustomers;
    private BigDecimal    avgOrderValue;
    private LocalDateTime generatedAt;
}

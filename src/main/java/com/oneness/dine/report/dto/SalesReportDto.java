package com.oneness.dine.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class SalesReportDto {
    private Long              branchId;
    private LocalDate         from;
    private LocalDate         to;
    private Integer           totalDays;
    private Integer           totalOrders;
    private BigDecimal        totalRevenue;
    private BigDecimal        totalDiscount;
    private BigDecimal        totalTax;
    private BigDecimal        avgOrderValue;
    private Integer           totalCustomers;
    private Integer           cancelledOrders;
    private List<DailySummaryDto> dailyBreakdown;
}

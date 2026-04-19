package com.oneness.dine.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ItemSalesDto {
    private Long       itemId;
    private String     itemName;
    private String     categoryName;
    private Integer    quantitySold;
    private BigDecimal totalRevenue;
    private BigDecimal avgPrice;
}

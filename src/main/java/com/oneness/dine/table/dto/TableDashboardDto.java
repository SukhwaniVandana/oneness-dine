package com.oneness.dine.table.dto;

import com.oneness.dine.core.enums.OrderStatus;   // [NEW]
import com.oneness.dine.core.enums.TableStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class TableDashboardDto {
    private Long              tableId;
    private String            tableNumber;
    private String            sectionName;
    private Integer           capacity;
    private TableStatus       tableStatus;
    private Boolean           isLocked;
    private Boolean           isMerged;
    private Long              mergedWithTableId;

    // Active orders on this table
    // Each order = one box on dashboard
    private List<TableOrderSummary> activeOrders;

    @Getter
    @Setter
    @Builder
    public static class TableOrderSummary {
        private Long          orderId;
        private String        orderSessionTag;
        private Integer       sessionOrderIndex;
        private LocalDateTime sessionStartedAt;   // for timer calculation
        private Integer       paxCount;
        private Boolean       isPrinted;           // physical print only
        private Boolean       isSettled;
        private Integer       itemCount;
        private String        waiterName;
        private OrderStatus   orderStatus;          // [NEW] — for YELLOW vs GREEN dashboard color
    }
}

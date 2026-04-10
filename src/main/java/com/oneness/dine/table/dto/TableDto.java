package com.oneness.dine.table.dto;

import com.oneness.dine.core.enums.TableStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TableDto {
    private Long        id;
    private Long        branchId;
    private Long        sectionId;
    private String      sectionName;
    private String      tableNumber;
    private Integer     capacity;
    private TableStatus tableStatus;
    private Boolean     isMerged;
    private Long        mergedWithTableId;
    private String      qrCodeUrl;
    private Long        waiterUserId;
    private String      waiterName;
    private Boolean     isLocked;
    private Boolean     isActive;
}
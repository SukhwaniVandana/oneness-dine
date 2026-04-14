package com.oneness.dine.order.dto;

import com.oneness.dine.core.enums.KotDisplayStatus;
import com.oneness.dine.core.enums.KotType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class KotDto {
    private Long             id;
    private Long             orderId;
    private String           orderNumber;
    private String           kotNumber;
    private KotType          kotType;
    private String           tableNumber;
    private Long             stationId;
    private String           stationName;
    private KotDisplayStatus displayStatus;
    private Boolean          isPrinted;
    private LocalDateTime    createdAt;
    private List<KotItemDto> items;
}
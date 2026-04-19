package com.oneness.dine.platform.dto;

import com.oneness.dine.core.enums.PlatformType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PlatformConfigDto {
    private Long          id;
    private Long          branchId;
    private PlatformType  platform;
    private String        storeId;
    private Boolean       isEnabled;
    private Boolean       autoAcceptOrders;
    private Boolean       menuSyncEnabled;
    private LocalDateTime lastMenuSyncAt;
    // Note: apiKey and webhookSecret intentionally excluded from response
}

package com.oneness.dine.device.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class KitchenSessionDto {
    private Long          id;
    private Long          branchId;
    private Long          deviceId;
    private String        terminalId;
    private Long          stationId;
    private String        stationName;
    private Long          userId;
    private String        sessionToken;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Boolean       isActive;
}

package com.oneness.dine.device.dto;

import com.oneness.dine.core.enums.DeviceType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DeviceDto {
    private Long          id;
    private Long          branchId;
    private String        terminalId;
    private DeviceType    deviceType;
    private String        deviceName;
    private String        deviceModel;
    private String        osInfo;
    private String        appVersion;
    private String        ipAddress;
    private LocalDateTime lastSeenAt;
    private Boolean       isActive;
    private LocalDateTime registeredAt;
}

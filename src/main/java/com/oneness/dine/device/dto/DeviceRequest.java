package com.oneness.dine.device.dto;

import com.oneness.dine.core.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceRequest {

    @NotBlank(message = "Terminal ID is required")
    private String terminalId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Device type is required")
    private DeviceType deviceType;

    private String deviceName;
    private String deviceModel;
    private String osInfo;
    private String appVersion;
    private String ipAddress;
}

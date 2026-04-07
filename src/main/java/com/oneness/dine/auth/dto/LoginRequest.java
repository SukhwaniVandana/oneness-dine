package com.oneness.dine.auth.dto;

import com.oneness.dine.core.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Terminal ID is required")
    private String terminalId;

    private DeviceType deviceType = DeviceType.DESKTOP;
    private String     deviceInfo;
    private Integer    screenNumber = 1;
}
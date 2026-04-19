package com.oneness.dine.device.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeartbeatRequest {
    private String appVersion;
    private String ipAddress;
}

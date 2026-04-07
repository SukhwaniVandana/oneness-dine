package com.oneness.dine.auth.dto;

import com.oneness.dine.core.enums.DeviceType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponse {

    private String     accessToken;
    private String     tokenType = "Bearer";
    private Long       expiresIn;
    private Long       userId;
    private String     username;
    private String     fullName;
    private String     roleName;
    private Long       branchId;
    private String     branchName;
    private DeviceType deviceType;
    private String     terminalId;
}
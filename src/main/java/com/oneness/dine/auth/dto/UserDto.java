package com.oneness.dine.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserDto {

    private Long          id;
    private String        fullName;
    private String        username;
    private String        roleName;
    private String        phone;
    private String        email;
    private Boolean       isActive;
    private LocalDateTime lastLoginAt;
    private Long          branchId;
    private String        branchName;
}
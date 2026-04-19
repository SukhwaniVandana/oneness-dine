package com.oneness.dine.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ActivityLogDto {
    private Long          id;
    private Long          branchId;
    private Long          userId;
    private String        userName;
    private Long          sessionId;
    private String        action;
    private String        entityType;
    private Long          entityId;
    private String        oldValue;
    private String        newValue;
    private String        ipAddress;
    private LocalDateTime createdAt;
}

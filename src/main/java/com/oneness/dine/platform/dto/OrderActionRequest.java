package com.oneness.dine.platform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderActionRequest {
    private String rejectionReason;
    private Long   branchId;
}

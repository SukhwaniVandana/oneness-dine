package com.oneness.dine.device.dto;

import com.oneness.dine.core.enums.ConnectionType;
import com.oneness.dine.core.enums.PrinterType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PrinterConfigDto {
    private Long           id;
    private Long           branchId;
    private String         printerName;
    private PrinterType    printerType;
    private ConnectionType connectionType;
    private String         ipAddress;
    private Integer        port;
    private String         terminalId;
    private Boolean        isDefault;
    private Boolean        isActive;
}

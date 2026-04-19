package com.oneness.dine.device.dto;

import com.oneness.dine.core.enums.ConnectionType;
import com.oneness.dine.core.enums.PrinterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrinterConfigRequest {

    @NotBlank(message = "Printer name is required")
    private String printerName;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotNull(message = "Printer type is required")
    private PrinterType printerType;

    @NotNull(message = "Connection type is required")
    private ConnectionType connectionType;

    private String  ipAddress;
    private Integer port;
    private String  terminalId;
    private Boolean isDefault = false;
}

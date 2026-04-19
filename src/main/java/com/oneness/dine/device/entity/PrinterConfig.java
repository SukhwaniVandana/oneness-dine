package com.oneness.dine.device.entity;

import com.oneness.dine.core.enums.ConnectionType;
import com.oneness.dine.core.enums.PrinterType;
import com.oneness.dine.restaurant.entity.Branch;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "printer_config")
public class PrinterConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "printer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "printer_name", nullable = false, length = 100)
    private String printerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "printer_type", nullable = false)
    private PrinterType printerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false)
    private ConnectionType connectionType;

    @Column(name = "ip_address", length = 20)
    private String ipAddress;

    @Column(name = "port")
    private Integer port;

    @Column(name = "terminal_id", length = 50)
    private String terminalId;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "is_active")
    private Boolean isActive = true;
}

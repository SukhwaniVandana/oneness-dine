package com.oneness.dine.device.repository;

import com.oneness.dine.core.enums.PrinterType;
import com.oneness.dine.device.entity.PrinterConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrinterConfigRepository
        extends JpaRepository<PrinterConfig, Long> {
    List<PrinterConfig> findByBranchIdAndIsActiveTrueOrderByPrinterNameAsc(
            Long branchId);
    List<PrinterConfig> findByTerminalIdAndIsActiveTrue(String terminalId);
    Optional<PrinterConfig> findByBranchIdAndPrinterTypeAndIsDefaultTrue(
            Long branchId, PrinterType printerType);
}

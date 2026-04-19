package com.oneness.dine.device.repository;

import com.oneness.dine.device.entity.DeviceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRegistrationRepository
        extends JpaRepository<DeviceRegistration, Long> {
    Optional<DeviceRegistration> findByTerminalId(String terminalId);
    List<DeviceRegistration> findByBranchIdAndIsActiveTrueOrderByDeviceNameAsc(
            Long branchId);
    boolean existsByTerminalId(String terminalId);
}

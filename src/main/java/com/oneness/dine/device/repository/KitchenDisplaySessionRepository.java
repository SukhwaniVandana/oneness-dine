package com.oneness.dine.device.repository;

import com.oneness.dine.device.entity.KitchenDisplaySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KitchenDisplaySessionRepository
        extends JpaRepository<KitchenDisplaySession, Long> {
    Optional<KitchenDisplaySession> findByDeviceIdAndIsActiveTrue(Long deviceId);
    List<KitchenDisplaySession> findByBranchIdAndIsActiveTrue(Long branchId);
    Optional<KitchenDisplaySession> findBySessionToken(String sessionToken);
}

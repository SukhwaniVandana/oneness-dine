package com.oneness.dine.platform.repository;

import com.oneness.dine.core.enums.PlatformOrderStatus;
import com.oneness.dine.core.enums.PlatformType;
import com.oneness.dine.platform.entity.PlatformOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformOrderRepository
        extends JpaRepository<PlatformOrder, Long> {

    List<PlatformOrder> findByBranchIdOrderByReceivedAtDesc(Long branchId);

    List<PlatformOrder> findByBranchIdAndOrderStatusOrderByReceivedAtDesc(
            Long branchId, PlatformOrderStatus status);

    List<PlatformOrder> findByBranchIdAndPlatformOrderByReceivedAtDesc(
            Long branchId, PlatformType platform);

    Optional<PlatformOrder> findByExternalOrderIdAndPlatform(
            String externalOrderId, PlatformType platform);

    boolean existsByExternalOrderIdAndPlatform(
            String externalOrderId, PlatformType platform);
}

package com.oneness.dine.platform.repository;

import com.oneness.dine.core.enums.PlatformType;
import com.oneness.dine.platform.entity.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformConfigRepository
        extends JpaRepository<PlatformConfig, Long> {

    List<PlatformConfig> findByBranchId(Long branchId);

    Optional<PlatformConfig> findByBranchIdAndPlatform(
            Long branchId, PlatformType platform);

    List<PlatformConfig> findByBranchIdAndIsEnabledTrue(Long branchId);
}

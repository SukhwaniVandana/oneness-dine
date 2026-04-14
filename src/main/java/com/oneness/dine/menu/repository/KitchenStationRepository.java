package com.oneness.dine.menu.repository;

import com.oneness.dine.menu.entity.KitchenStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitchenStationRepository
        extends JpaRepository<KitchenStation, Long> {
    List<KitchenStation> findByBranchIdAndIsActiveTrue(Long branchId);
}
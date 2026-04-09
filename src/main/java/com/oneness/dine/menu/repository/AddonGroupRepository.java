package com.oneness.dine.menu.repository;

import com.oneness.dine.menu.entity.AddonGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddonGroupRepository
        extends JpaRepository<AddonGroup, Long> {
    List<AddonGroup> findByBranchId(Long branchId);
}
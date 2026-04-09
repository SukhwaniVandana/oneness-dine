package com.oneness.dine.menu.repository;

import com.oneness.dine.menu.entity.TaxGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxGroupRepository
        extends JpaRepository<TaxGroup, Long> {
    List<TaxGroup> findByBranchIdAndIsActiveTrue(Long branchId);
    boolean existsByBranchIdAndName(Long branchId, String name);
}
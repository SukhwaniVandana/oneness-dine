package com.oneness.dine.menu.repository;

import com.oneness.dine.menu.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuCategoryRepository
        extends JpaRepository<MenuCategory, Long> {
    List<MenuCategory> findByBranchIdAndIsActiveTrueOrderByDisplayOrderAsc(Long branchId);
    List<MenuCategory> findByBranchIdAndParentCategoryIsNullAndIsActiveTrueOrderByDisplayOrderAsc(Long branchId);
    boolean existsByBranchIdAndName(Long branchId, String name);
}
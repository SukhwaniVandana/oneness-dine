package com.oneness.dine.menu.repository;

import com.oneness.dine.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository
        extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByBranchIdAndIsActiveTrueOrderByDisplayOrderAsc(
            Long branchId);

    List<MenuItem> findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(
            Long categoryId);

    List<MenuItem> findByBranchIdAndIsAvailableTrueAndIsActiveTrueOrderByDisplayOrderAsc(
            Long branchId);

    @Query("SELECT m FROM MenuItem m WHERE m.branch.id = :branchId " +
            "AND m.isActive = true " +
            "AND LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MenuItem> searchByName(@Param("branchId") Long branchId,
                                @Param("keyword") String keyword);

    boolean existsByItemCode(String itemCode);
}
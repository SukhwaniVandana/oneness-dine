package com.oneness.dine.inventory.repository;

import com.oneness.dine.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryItemRepository
        extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByBranchIdAndIsActiveTrueOrderByNameAsc(
            Long branchId);

    // Performance: single query for low stock items
    @Query("SELECT i FROM InventoryItem i " +
           "WHERE i.branch.id = :branchId " +
           "AND i.isActive = true " +
           "AND i.currentStock <= i.minimumStock " +
           "ORDER BY i.currentStock ASC")
    List<InventoryItem> findLowStockItems(@Param("branchId") Long branchId);

    boolean existsByBranchIdAndName(Long branchId, String name);
}

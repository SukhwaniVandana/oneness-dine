package com.oneness.dine.table.repository;

import com.oneness.dine.core.enums.TableStatus;
import com.oneness.dine.table.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantTableRepository 
        extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByBranchIdAndIsActiveTrueOrderByTableNumberAsc(
            Long branchId);

    List<RestaurantTable> findBySectionIdAndIsActiveTrueOrderByTableNumberAsc(
            Long sectionId);

    List<RestaurantTable> findByBranchIdAndTableStatusAndIsActiveTrue(
            Long branchId, TableStatus status);

    boolean existsByBranchIdAndTableNumber(
            Long branchId, String tableNumber);

    @Query("SELECT t FROM RestaurantTable t " +
           "WHERE t.branch.id = :branchId " +
           "AND t.isActive = true " +
           "ORDER BY t.section.displayOrder ASC, t.tableNumber ASC")
    List<RestaurantTable> findDashboardTables(
            @Param("branchId") Long branchId);
}
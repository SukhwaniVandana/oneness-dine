package com.oneness.dine.order.repository;

import com.oneness.dine.core.enums.OrderStatus;
import com.oneness.dine.order.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    // Active orders on a table (not settled)
    List<CustomerOrder> findByTableIdAndIsSettledFalseAndDeletedAtIsNull(
            Long tableId);

    // Orders by session tag
    List<CustomerOrder> findByOrderSessionTag(String orderSessionTag);

    // All active orders for branch
    List<CustomerOrder> findByBranchIdAndIsSettledFalseAndDeletedAtIsNull(
            Long branchId);

    // Today's orders
    @Query("SELECT o FROM CustomerOrder o " +
           "WHERE o.branch.id = :branchId " +
           "AND o.createdAt >= :startOfDay " +
           "AND o.deletedAt IS NULL " +
           "ORDER BY o.createdAt DESC")
    List<CustomerOrder> findTodaysOrders(
            @Param("branchId") Long branchId,
            @Param("startOfDay") LocalDateTime startOfDay);

    // Count active orders on table for session index
    @Query("SELECT COUNT(o) FROM CustomerOrder o " +
           "WHERE o.table.id = :tableId " +
           "AND o.isSettled = false " +
           "AND o.deletedAt IS NULL")
    Integer countActiveOrdersOnTable(@Param("tableId") Long tableId);

    // Find by order number
    java.util.Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    // Check if order number exists
    boolean existsByOrderNumber(String orderNumber);
}
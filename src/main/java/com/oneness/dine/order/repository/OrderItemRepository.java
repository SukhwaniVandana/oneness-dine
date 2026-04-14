package com.oneness.dine.order.repository;

import com.oneness.dine.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi " +
           "WHERE oi.order.id = :orderId " +
           "AND oi.kotQuantity < oi.quantity")
    List<OrderItem> findPendingKotItems(
            @Param("orderId") Long orderId);
}
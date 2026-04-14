package com.oneness.dine.order.repository;

import com.oneness.dine.order.entity.OrderItemAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemAddonRepository
        extends JpaRepository<OrderItemAddon, Long> {
    List<OrderItemAddon> findByOrderItemId(Long orderItemId);
}
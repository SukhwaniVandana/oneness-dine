package com.oneness.dine.order.repository;

import com.oneness.dine.order.entity.KotItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KotItemRepository
        extends JpaRepository<KotItem, Long> {
    List<KotItem> findByKotId(Long kotId);
    List<KotItem> findByOrderItemId(Long orderItemId);
}
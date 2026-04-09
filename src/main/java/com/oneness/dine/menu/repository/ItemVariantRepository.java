package com.oneness.dine.menu.repository;

import com.oneness.dine.menu.entity.ItemVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemVariantRepository
        extends JpaRepository<ItemVariant, Long> {
    List<ItemVariant> findByItemIdOrderByIsDefaultDesc(Long itemId);
    void deleteByItemId(Long itemId);
}
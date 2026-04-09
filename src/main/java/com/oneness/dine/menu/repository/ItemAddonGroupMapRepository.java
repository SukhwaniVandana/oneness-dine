package com.oneness.dine.menu.repository;

import com.oneness.dine.menu.entity.ItemAddonGroupMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemAddonGroupMapRepository
        extends JpaRepository<ItemAddonGroupMap, Long> {
    List<ItemAddonGroupMap> findByItemId(Long itemId);
    void deleteByItemId(Long itemId);
    boolean existsByItemIdAndAddonGroupId(Long itemId, Long addonGroupId);
}
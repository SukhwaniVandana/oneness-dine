package com.oneness.dine.menu.repository;

import com.oneness.dine.menu.entity.AddonItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddonItemRepository
        extends JpaRepository<AddonItem, Long> {
    List<AddonItem> findByAddonGroupId(Long addonGroupId);
    void deleteByAddonGroupId(Long addonGroupId);
}
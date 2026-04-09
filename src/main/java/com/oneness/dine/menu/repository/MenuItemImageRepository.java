package com.oneness.dine.menu.repository;

import com.oneness.dine.menu.entity.MenuItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemImageRepository
        extends JpaRepository<MenuItemImage, Long> {
    List<MenuItemImage> findByItemIdOrderByDisplayOrderAsc(Long itemId);
    Optional<MenuItemImage> findByItemIdAndIsPrimaryTrue(Long itemId);
    void deleteByItemId(Long itemId);
}
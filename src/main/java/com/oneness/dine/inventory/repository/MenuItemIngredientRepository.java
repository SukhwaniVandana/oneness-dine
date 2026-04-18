package com.oneness.dine.inventory.repository;

import com.oneness.dine.inventory.entity.MenuItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemIngredientRepository
        extends JpaRepository<MenuItemIngredient, Long> {
    List<MenuItemIngredient> findByItemId(Long itemId);
    void deleteByItemId(Long itemId);
}

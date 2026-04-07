package com.oneness.dine.restaurant.repository;

import com.oneness.dine.restaurant.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository
        extends JpaRepository<Branch, Long> {
    List<Branch> findByRestaurantIdAndIsActiveTrue(Long restaurantId);
    List<Branch> findByIsActiveTrue();
}
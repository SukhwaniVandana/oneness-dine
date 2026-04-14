package com.oneness.dine.order.repository;

import com.oneness.dine.core.enums.KotDisplayStatus;
import com.oneness.dine.order.entity.Kot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KotRepository
        extends JpaRepository<Kot, Long> {

    List<Kot> findByOrderId(Long orderId);

    List<Kot> findByBranchIdAndDisplayStatusOrderByCreatedAtAsc(
            Long branchId, KotDisplayStatus status);

    List<Kot> findByBranchIdAndDisplayStatusInOrderByCreatedAtAsc(
            Long branchId, List<KotDisplayStatus> statuses);

    // Latest KOT number for generating next number
    java.util.Optional<Kot> findTopByBranchIdOrderByCreatedAtDesc(
            Long branchId);
}
package com.oneness.dine.customer.repository;

import com.oneness.dine.customer.entity.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyTransactionRepository
        extends JpaRepository<LoyaltyTransaction, Long> {
    List<LoyaltyTransaction> findByCustomerIdOrderByCreatedAtDesc(
            Long customerId);
}

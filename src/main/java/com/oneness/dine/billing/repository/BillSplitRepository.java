package com.oneness.dine.billing.repository;

import com.oneness.dine.billing.entity.BillSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillSplitRepository extends JpaRepository<BillSplit, Long> {
    List<BillSplit> findByOriginalBillId(Long originalBillId);
}

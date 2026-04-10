package com.oneness.dine.table.repository;

import com.oneness.dine.table.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository 
        extends JpaRepository<Section, Long> {
    List<Section> findByBranchIdAndIsActiveTrueOrderByDisplayOrderAsc(
            Long branchId);
    boolean existsByBranchIdAndName(Long branchId, String name);
}
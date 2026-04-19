package com.oneness.dine.report.repository;

import com.oneness.dine.report.entity.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailySummaryRepository
        extends JpaRepository<DailySummary, Long> {

    Optional<DailySummary> findByBranchIdAndSummaryDate(
            Long branchId, LocalDate date);

    // Performance: date range query with index on summary_date
    @Query("SELECT d FROM DailySummary d " +
           "WHERE d.branch.id = :branchId " +
           "AND d.summaryDate BETWEEN :from AND :to " +
           "ORDER BY d.summaryDate DESC")
    List<DailySummary> findByBranchIdAndDateRange(
            @Param("branchId") Long branchId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}

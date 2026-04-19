package com.oneness.dine.report.repository;

import com.oneness.dine.report.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository
        extends JpaRepository<ActivityLog, Long> {

    // Performance: paginated query to avoid loading all logs
    @Query("SELECT a FROM ActivityLog a " +
           "WHERE a.branch.id = :branchId " +
           "AND a.createdAt BETWEEN :from AND :to " +
           "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByBranchAndDateRange(
            @Param("branchId") Long branchId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    List<ActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId);
}

package com.oneness.dine.table.service;

import com.oneness.dine.auth.entity.UserSession;
import com.oneness.dine.auth.repository.UserSessionRepository;
import com.oneness.dine.core.enums.TableStatus;
import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.restaurant.entity.Branch;
import com.oneness.dine.restaurant.repository.BranchRepository;
import com.oneness.dine.table.dto.*;
import com.oneness.dine.table.entity.RestaurantTable;
import com.oneness.dine.table.entity.Section;
import com.oneness.dine.table.repository.RestaurantTableRepository;
import com.oneness.dine.table.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableService {

    private final SectionRepository          sectionRepository;
    private final RestaurantTableRepository  tableRepository;
    private final BranchRepository           branchRepository;
    private final UserSessionRepository      sessionRepository;

    private Branch getBranch(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", branchId));
    }

    // ─── SECTION ─────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SectionDto> getAllSections(Long branchId) {
        return sectionRepository
                .findByBranchIdAndIsActiveTrueOrderByDisplayOrderAsc(branchId)
                .stream().map(this::toSectionDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SectionDto createSection(SectionRequest request) {
        if (sectionRepository.existsByBranchIdAndName(
                request.getBranchId(), request.getName())) {
            throw new BusinessException(
                    "Section with this name already exists",
                    "SECTION_EXISTS");
        }
        Section section = new Section();
        section.setBranch(getBranch(request.getBranchId()));
        section.setName(request.getName());
        section.setDisplayOrder(request.getDisplayOrder());
        section.setIsActive(true);
        log.info("Section created: {}", request.getName());
        return toSectionDto(sectionRepository.save(section));
    }

    @Transactional
    public SectionDto updateSection(Long id, SectionRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Section", "id", id));
        if (request.getName()         != null) section.setName(request.getName());
        if (request.getDisplayOrder() != null) section.setDisplayOrder(request.getDisplayOrder());
        return toSectionDto(sectionRepository.save(section));
    }

    @Transactional
    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Section", "id", id));
        section.setIsActive(false);
        sectionRepository.save(section);
        log.info("Section soft deleted: {}", id);
    }

    // ─── TABLE ───────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TableDto> getAllTables(Long branchId) {
        return tableRepository
                .findByBranchIdAndIsActiveTrueOrderByTableNumberAsc(branchId)
                .stream().map(this::toTableDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TableDto> getTablesBySection(Long sectionId) {
        return tableRepository
                .findBySectionIdAndIsActiveTrueOrderByTableNumberAsc(sectionId)
                .stream().map(this::toTableDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TableDto getTableById(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Table", "id", id));
        return toTableDto(table);
    }

    @Transactional
    public TableDto createTable(TableRequest request) {
        if (tableRepository.existsByBranchIdAndTableNumber(
                request.getBranchId(), request.getTableNumber())) {
            throw new BusinessException(
                    "Table number already exists in this branch",
                    "TABLE_NUMBER_EXISTS");
        }
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Section", "id", request.getSectionId()));

        RestaurantTable table = new RestaurantTable();
        table.setBranch(getBranch(request.getBranchId()));
        table.setSection(section);
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setTableStatus(TableStatus.AVAILABLE);
        table.setIsMerged(false);
        table.setIsActive(true);

        log.info("Table created: {}", request.getTableNumber());
        return toTableDto(tableRepository.save(table));
    }

    @Transactional
    public TableDto updateTable(Long id, TableRequest request) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Table", "id", id));
        if (request.getTableNumber() != null)
            table.setTableNumber(request.getTableNumber());
        if (request.getCapacity() != null)
            table.setCapacity(request.getCapacity());
        if (request.getSectionId() != null) {
            Section section = sectionRepository
                    .findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Section", "id", request.getSectionId()));
            table.setSection(section);
        }
        return toTableDto(tableRepository.save(table));
    }

    @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Table", "id", id));
        table.setIsActive(false);
        tableRepository.save(table);
        log.info("Table soft deleted: {}", id);
    }

    // ─── TABLE LOCKING ───────────────────────────────

    @Transactional
    public TableDto lockTable(Long tableId, Long sessionId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Table", "id", tableId));

        // Check if already locked by another session
        if (table.getLockedBySession() != null &&
                !table.getLockedBySession().getId().equals(sessionId)) {
            throw new BusinessException(
                    "Table is being edited on another terminal",
                    "TABLE_LOCKED");
        }

        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session", "id", sessionId));

        table.setLockedBySession(session);
        table.setLockedAt(LocalDateTime.now());
        log.info("Table {} locked by session {}", tableId, sessionId);
        return toTableDto(tableRepository.save(table));
    }

    @Transactional
    public TableDto unlockTable(Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Table", "id", tableId));
        table.setLockedBySession(null);
        table.setLockedAt(null);
        log.info("Table {} unlocked", tableId);
        return toTableDto(tableRepository.save(table));
    }

    // ─── TABLE MERGE ─────────────────────────────────

    @Transactional
    public void mergeTables(Long primaryTableId, Long secondaryTableId) {
        RestaurantTable primary = tableRepository.findById(primaryTableId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Table", "id", primaryTableId));
        RestaurantTable secondary = tableRepository.findById(secondaryTableId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Table", "id", secondaryTableId));

        secondary.setIsMerged(true);
        secondary.setMergedWithTable(primary);
        secondary.setTableStatus(TableStatus.MERGED);
        tableRepository.save(secondary);

        primary.setTableStatus(TableStatus.OCCUPIED);
        tableRepository.save(primary);

        log.info("Tables merged: {} + {}", primaryTableId, secondaryTableId);
    }

    @Transactional
    public void unmergeTables(Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Table", "id", tableId));
        table.setIsMerged(false);
        table.setMergedWithTable(null);
        table.setTableStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);
        log.info("Table unmerged: {}", tableId);
    }

    // ─── DASHBOARD ───────────────────────────────────

    @Transactional(readOnly = true)
    public List<TableDashboardDto> getDashboard(Long branchId) {
        return tableRepository.findDashboardTables(branchId)
                .stream()
                .map(table -> TableDashboardDto.builder()
                        .tableId(table.getId())
                        .tableNumber(table.getTableNumber())
                        .sectionName(table.getSection().getName())
                        .capacity(table.getCapacity())
                        .tableStatus(table.getTableStatus())
                        .isLocked(table.getLockedBySession() != null)
                        .isMerged(table.getIsMerged())
                        .mergedWithTableId(table.getMergedWithTable() != null ?
                                table.getMergedWithTable().getId() : null)
                        .activeOrders(List.of())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── MAPPERS ─────────────────────────────────────

    private SectionDto toSectionDto(Section s) {
        return SectionDto.builder()
                .id(s.getId())
                .branchId(s.getBranch().getId())
                .name(s.getName())
                .displayOrder(s.getDisplayOrder())
                .isActive(s.getIsActive())
                .build();
    }

    private TableDto toTableDto(RestaurantTable t) {
        return TableDto.builder()
                .id(t.getId())
                .branchId(t.getBranch().getId())
                .sectionId(t.getSection().getId())
                .sectionName(t.getSection().getName())
                .tableNumber(t.getTableNumber())
                .capacity(t.getCapacity())
                .tableStatus(t.getTableStatus())
                .isMerged(t.getIsMerged())
                .mergedWithTableId(t.getMergedWithTable() != null ?
                        t.getMergedWithTable().getId() : null)
                .qrCodeUrl(t.getQrCodeUrl())
                .waiterUserId(t.getWaiterUser() != null ?
                        t.getWaiterUser().getId() : null)
                .waiterName(t.getWaiterUser() != null ?
                        t.getWaiterUser().getFullName() : null)
                .isLocked(t.getLockedBySession() != null)
                .isActive(t.getIsActive())
                .build();
    }
}
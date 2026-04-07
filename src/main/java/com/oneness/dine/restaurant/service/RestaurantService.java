package com.oneness.dine.restaurant.service;

import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.restaurant.dto.*;
import com.oneness.dine.restaurant.entity.Branch;
import com.oneness.dine.restaurant.entity.BranchSettings;
import com.oneness.dine.restaurant.entity.Restaurant;
import com.oneness.dine.restaurant.repository.BranchRepository;
import com.oneness.dine.restaurant.repository.BranchSettingsRepository;
import com.oneness.dine.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository   restaurantRepository;
    private final BranchRepository       branchRepository;
    private final BranchSettingsRepository branchSettingsRepository;

    // ─── RESTAURANT ──────────────────────────────────

    @Transactional(readOnly = true)
    public RestaurantDto getRestaurant(Long id) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant", "id", id));
        return toRestaurantDto(r);
    }

    @Transactional
    public RestaurantDto updateRestaurant(Long id, RestaurantDto dto) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant", "id", id));
        if (dto.getName()        != null) r.setName(dto.getName());
        if (dto.getLegalName()   != null) r.setLegalName(dto.getLegalName());
        if (dto.getGstin()       != null) r.setGstin(dto.getGstin());
        if (dto.getPanNumber()   != null) r.setPanNumber(dto.getPanNumber());
        if (dto.getFssaiNumber() != null) r.setFssaiNumber(dto.getFssaiNumber());
        if (dto.getEmail()       != null) r.setEmail(dto.getEmail());
        if (dto.getPhone()       != null) r.setPhone(dto.getPhone());
        if (dto.getLogoUrl()     != null) r.setLogoUrl(dto.getLogoUrl());
        return toRestaurantDto(restaurantRepository.save(r));
    }

    // ─── BRANCH ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BranchDto> getAllBranches() {
        return branchRepository.findByIsActiveTrue()
                .stream().map(this::toBranchDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BranchDto getBranch(Long id) {
        Branch b = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", id));
        return toBranchDto(b);
    }

    @Transactional
    public BranchDto createBranch(BranchRequest request) {
        Restaurant restaurant = restaurantRepository
                .findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant", "id", request.getRestaurantId()));

        Branch b = new Branch();
        b.setRestaurant(restaurant);
        b.setBranchName(request.getBranchName());
        b.setAddress(request.getAddress());
        b.setCity(request.getCity());
        b.setState(request.getState());
        b.setPincode(request.getPincode());
        b.setPhone(request.getPhone());
        b.setGstin(request.getGstin());
        b.setOpeningTime(request.getOpeningTime());
        b.setClosingTime(request.getClosingTime());
        b.setIs24Hours(request.getIs24Hours());
        b.setSupportsDineIn(request.getSupportsDineIn());
        b.setSupportsTakeaway(request.getSupportsTakeaway());
        b.setSupportsDelivery(request.getSupportsDelivery());
        b.setIsActive(true);

        Branch saved = branchRepository.save(b);

        // Auto-create default settings for new branch
        BranchSettings settings = new BranchSettings();
        settings.setBranch(saved);
        branchSettingsRepository.save(settings);

        log.info("Branch created: {}", saved.getBranchName());
        return toBranchDto(saved);
    }

    @Transactional
    public BranchDto updateBranch(Long id, BranchRequest request) {
        Branch b = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", id));
        if (request.getBranchName()    != null) b.setBranchName(request.getBranchName());
        if (request.getAddress()       != null) b.setAddress(request.getAddress());
        if (request.getCity()          != null) b.setCity(request.getCity());
        if (request.getState()         != null) b.setState(request.getState());
        if (request.getPincode()       != null) b.setPincode(request.getPincode());
        if (request.getPhone()         != null) b.setPhone(request.getPhone());
        if (request.getOpeningTime()   != null) b.setOpeningTime(request.getOpeningTime());
        if (request.getClosingTime()   != null) b.setClosingTime(request.getClosingTime());
        return toBranchDto(branchRepository.save(b));
    }

    // ─── BRANCH SETTINGS ─────────────────────────────

    @Transactional(readOnly = true)
    public BranchSettingsDto getBranchSettings(Long branchId) {
        BranchSettings s = branchSettingsRepository
                .findByBranchId(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BranchSettings", "branchId", branchId));
        return toSettingsDto(s);
    }

    @Transactional
    public BranchSettingsDto updateBranchSettings(
            Long branchId, BranchSettingsDto dto) {
        BranchSettings s = branchSettingsRepository
                .findByBranchId(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BranchSettings", "branchId", branchId));

        if (dto.getKotPrintMode()          != null) s.setKotPrintMode(dto.getKotPrintMode());
        if (dto.getBillPrintCopies()       != null) s.setBillPrintCopies(dto.getBillPrintCopies());
        if (dto.getServiceChargePct()      != null) s.setServiceChargePct(dto.getServiceChargePct());
        if (dto.getRoundOffEnabled()       != null) s.setRoundOffEnabled(dto.getRoundOffEnabled());
        if (dto.getLoyaltyEnabled()        != null) s.setLoyaltyEnabled(dto.getLoyaltyEnabled());
        if (dto.getTableMergeEnabled()     != null) s.setTableMergeEnabled(dto.getTableMergeEnabled());
        if (dto.getSplitBillEnabled()      != null) s.setSplitBillEnabled(dto.getSplitBillEnabled());
        if (dto.getMultiOrderPerTable()    != null) s.setMultiOrderPerTable(dto.getMultiOrderPerTable());
        if (dto.getSwiggyEnabled()         != null) s.setSwiggyEnabled(dto.getSwiggyEnabled());
        if (dto.getZomatoEnabled()         != null) s.setZomatoEnabled(dto.getZomatoEnabled());
        if (dto.getDualScreenMode()        != null) s.setDualScreenMode(dto.getDualScreenMode());
        if (dto.getCaptainAppEnabled()     != null) s.setCaptainAppEnabled(dto.getCaptainAppEnabled());
        if (dto.getKitchenDisplayEnabled() != null) s.setKitchenDisplayEnabled(dto.getKitchenDisplayEnabled());

        return toSettingsDto(branchSettingsRepository.save(s));
    }

    // ─── MAPPERS ─────────────────────────────────────

    private RestaurantDto toRestaurantDto(Restaurant r) {
        return RestaurantDto.builder()
                .id(r.getId())
                .name(r.getName())
                .legalName(r.getLegalName())
                .gstin(r.getGstin())
                .panNumber(r.getPanNumber())
                .fssaiNumber(r.getFssaiNumber())
                .email(r.getEmail())
                .phone(r.getPhone())
                .logoUrl(r.getLogoUrl())
                .currency(r.getCurrency())
                .timezone(r.getTimezone())
                .isActive(r.getIsActive())
                .build();
    }

    private BranchDto toBranchDto(Branch b) {
        return BranchDto.builder()
                .id(b.getId())
                .branchName(b.getBranchName())
                .address(b.getAddress())
                .city(b.getCity())
                .state(b.getState())
                .pincode(b.getPincode())
                .phone(b.getPhone())
                .gstin(b.getGstin())
                .openingTime(b.getOpeningTime())
                .closingTime(b.getClosingTime())
                .is24Hours(b.getIs24Hours())
                .supportsDineIn(b.getSupportsDineIn())
                .supportsTakeaway(b.getSupportsTakeaway())
                .supportsDelivery(b.getSupportsDelivery())
                .isActive(b.getIsActive())
                .restaurantId(b.getRestaurant().getId())
                .build();
    }

    private BranchSettingsDto toSettingsDto(BranchSettings s) {
        return BranchSettingsDto.builder()
                .id(s.getId())
                .branchId(s.getBranch().getId())
                .kotPrintMode(s.getKotPrintMode())
                .billPrintCopies(s.getBillPrintCopies())
                .serviceChargePct(s.getServiceChargePct())
                .roundOffEnabled(s.getRoundOffEnabled())
                .loyaltyEnabled(s.getLoyaltyEnabled())
                .tableMergeEnabled(s.getTableMergeEnabled())
                .splitBillEnabled(s.getSplitBillEnabled())
                .multiOrderPerTable(s.getMultiOrderPerTable())
                .swiggyEnabled(s.getSwiggyEnabled())
                .zomatoEnabled(s.getZomatoEnabled())
                .dualScreenMode(s.getDualScreenMode())
                .captainAppEnabled(s.getCaptainAppEnabled())
                .kitchenDisplayEnabled(s.getKitchenDisplayEnabled())
                .build();
    }
}
package com.oneness.dine.menu.service;

import com.oneness.dine.core.exception.BusinessException;
import com.oneness.dine.core.exception.ResourceNotFoundException;
import com.oneness.dine.menu.dto.*;
import com.oneness.dine.menu.entity.*;
import com.oneness.dine.menu.repository.*;
import com.oneness.dine.restaurant.entity.Branch;
import com.oneness.dine.restaurant.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuCategoryRepository    categoryRepository;
    private final MenuItemRepository        itemRepository;
    private final ItemVariantRepository     variantRepository;
    private final AddonGroupRepository      addonGroupRepository;
    private final AddonItemRepository       addonItemRepository;
    private final ItemAddonGroupMapRepository addonMapRepository;
    private final TaxGroupRepository        taxGroupRepository;
    private final BranchRepository          branchRepository;
    private final MenuItemImageRepository imageRepository;

    @Value("${oneness.upload.menu-images}")
    private String menuImagePath;
    // ─── HELPER ──────────────────────────────────────

    private Branch getBranch(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch", "id", branchId));
    }

    // ─── TAX GROUP ───────────────────────────────────

    @Transactional(readOnly = true)
    public List<TaxGroupDto> getAllTaxGroups(Long branchId) {
        return taxGroupRepository
                .findByBranchIdAndIsActiveTrue(branchId)
                .stream().map(this::toTaxGroupDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaxGroupDto createTaxGroup(TaxGroupRequest request) {
        if (taxGroupRepository.existsByBranchIdAndName(
                request.getBranchId(), request.getName())) {
            throw new BusinessException(
                    "Tax group with this name already exists",
                    "TAX_GROUP_EXISTS");
        }
        TaxGroup tg = new TaxGroup();
        tg.setBranch(getBranch(request.getBranchId()));
        tg.setName(request.getName());
        tg.setCgstPct(request.getCgstPct());
        tg.setSgstPct(request.getSgstPct());
        tg.setIgstPct(request.getIgstPct());
        tg.setCessPct(request.getCessPct());
        tg.setIsInclusive(request.getIsInclusive());
        tg.setIsActive(true);
        return toTaxGroupDto(taxGroupRepository.save(tg));
    }

    @Transactional
    public void deleteTaxGroup(Long id) {
        TaxGroup tg = taxGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TaxGroup", "id", id));
        tg.setIsActive(false);
        taxGroupRepository.save(tg);
        log.info("Tax group soft deleted: {}", id);
    }

    // ─── CATEGORY ────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories(Long branchId) {
        return categoryRepository
                .findByBranchIdAndIsActiveTrueOrderByDisplayOrderAsc(branchId)
                .stream().map(this::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto createCategory(CategoryRequest request) {
        if (categoryRepository.existsByBranchIdAndName(
                request.getBranchId(), request.getName())) {
            throw new BusinessException(
                    "Category with this name already exists",
                    "CATEGORY_EXISTS");
        }
        MenuCategory category = new MenuCategory();
        category.setBranch(getBranch(request.getBranchId()));
        category.setName(request.getName());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setColorCode(request.getColorCode());
        category.setIsActive(true);

        if (request.getParentCategoryId() != null) {
            MenuCategory parent = categoryRepository
                    .findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", "id", request.getParentCategoryId()));
            category.setParentCategory(parent);
        }

        return toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryRequest request) {
        MenuCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "id", id));
        if (request.getName()         != null) category.setName(request.getName());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getColorCode()    != null) category.setColorCode(request.getColorCode());
        return toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        MenuCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "id", id));
        category.setIsActive(false);
        categoryRepository.save(category);
        log.info("Category soft deleted: {}", id);
    }

    // ─── MENU ITEM ───────────────────────────────────

    @Transactional(readOnly = true)
    public List<MenuItemDto> getAllItems(Long branchId) {
        return itemRepository
                .findByBranchIdAndIsActiveTrueOrderByDisplayOrderAsc(branchId)
                .stream().map(this::toMenuItemDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemDto> getItemsByCategory(Long categoryId) {
        return itemRepository
                .findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId)
                .stream().map(this::toMenuItemDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuItemDto getItemById(Long id) {
        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MenuItem", "id", id));
        return toMenuItemDtoWithDetails(item);
    }

    @Transactional(readOnly = true)
    public List<MenuItemDto> searchItems(Long branchId, String keyword) {
        return itemRepository.searchByName(branchId, keyword)
                .stream().map(this::toMenuItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItemDto createItem(MenuItemRequest request) {
        if (request.getItemCode() != null &&
                itemRepository.existsByItemCode(request.getItemCode())) {
            throw new BusinessException(
                    "Item with this code already exists",
                    "ITEM_CODE_EXISTS");
        }

        MenuCategory category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "id", request.getCategoryId()));

        MenuItem item = new MenuItem();
        item.setBranch(getBranch(request.getBranchId()));
        item.setCategory(category);
        item.setName(request.getName());
        item.setShortName(request.getShortName());
        item.setDescription(request.getDescription());
        item.setFoodType(request.getFoodType());
        item.setBasePrice(request.getBasePrice());
        item.setItemCode(request.getItemCode());
        item.setHasVariants(request.getHasVariants());
        item.setHasAddons(request.getHasAddons());
        item.setIsRecommended(request.getIsRecommended());
        item.setIsBestseller(request.getIsBestseller());
        item.setIsSeasonal(request.getIsSeasonal());
        item.setAvailableFromTime(request.getAvailableFromTime());
        item.setAvailableToTime(request.getAvailableToTime());
        item.setDisplayOrder(request.getDisplayOrder());
        // [REMOVED] ───────────────────────────────────
        // item.setImageUrl(request.getImageUrl());
        // [REMOVED] ───────────────────────────────────
        item.setIsAvailable(true);
        item.setIsActive(true);

        if (request.getTaxGroupId() != null) {
            TaxGroup taxGroup = taxGroupRepository
                    .findById(request.getTaxGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "TaxGroup", "id", request.getTaxGroupId()));
            item.setTaxGroup(taxGroup);
        }

        MenuItem saved = itemRepository.save(item);
        log.info("Menu item created: {}", saved.getName());
        return toMenuItemDto(saved);
    }

    @Transactional
    public MenuItemDto updateItem(Long id, MenuItemRequest request) {
        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MenuItem", "id", id));

        if (request.getName()             != null) item.setName(request.getName());
        if (request.getShortName()        != null) item.setShortName(request.getShortName());
        if (request.getDescription()      != null) item.setDescription(request.getDescription());
        if (request.getFoodType()         != null) item.setFoodType(request.getFoodType());
        if (request.getBasePrice()        != null) item.setBasePrice(request.getBasePrice());
        if (request.getIsRecommended()    != null) item.setIsRecommended(request.getIsRecommended());
        if (request.getIsBestseller()     != null) item.setIsBestseller(request.getIsBestseller());
        if (request.getDisplayOrder()     != null) item.setDisplayOrder(request.getDisplayOrder());
        if (request.getAvailableFromTime()!= null) item.setAvailableFromTime(request.getAvailableFromTime());
        if (request.getAvailableToTime()  != null) item.setAvailableToTime(request.getAvailableToTime());

        if (request.getCategoryId() != null) {
            MenuCategory category = categoryRepository
                    .findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", "id", request.getCategoryId()));
            item.setCategory(category);
        }

        if (request.getTaxGroupId() != null) {
            TaxGroup taxGroup = taxGroupRepository
                    .findById(request.getTaxGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "TaxGroup", "id", request.getTaxGroupId()));
            item.setTaxGroup(taxGroup);
        }

        return toMenuItemDto(itemRepository.save(item));
    }

    @Transactional
    public MenuItemDto toggleAvailability(Long id,
                                          ItemAvailabilityRequest request) {
        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MenuItem", "id", id));
        item.setIsAvailable(request.getIsAvailable());
        log.info("Item {} availability set to: {}",
                item.getName(), request.getIsAvailable());
        return toMenuItemDto(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id) {
        MenuItem item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MenuItem", "id", id));
        item.setIsActive(false);
        itemRepository.save(item);
        log.info("Menu item soft deleted: {}", id);
    }

    // ─── VARIANTS ────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ItemVariantDto> getVariants(Long itemId) {
        return variantRepository
                .findByItemIdOrderByIsDefaultDesc(itemId)
                .stream().map(this::toVariantDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemVariantDto addVariant(Long itemId,
                                     ItemVariantRequest request) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MenuItem", "id", itemId));

        ItemVariant variant = new ItemVariant();
        variant.setItem(item);
        variant.setVariantName(request.getVariantName());
        variant.setPrice(request.getPrice());
        variant.setIsDefault(request.getIsDefault());
        variant.setIsAvailable(request.getIsAvailable());

        // update hasVariants flag
        item.setHasVariants(true);
        itemRepository.save(item);

        return toVariantDto(variantRepository.save(variant));
    }

    @Transactional
    public void deleteVariant(Long variantId) {
        variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ItemVariant", "id", variantId));
        variantRepository.deleteById(variantId);
    }

    // ─── ADDON GROUPS ────────────────────────────────

    @Transactional(readOnly = true)
    public List<AddonGroupDto> getAllAddonGroups(Long branchId) {
        return addonGroupRepository.findByBranchId(branchId)
                .stream().map(this::toAddonGroupDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddonGroupDto createAddonGroup(AddonGroupRequest request) {
        AddonGroup group = new AddonGroup();
        group.setBranch(getBranch(request.getBranchId()));
        group.setName(request.getName());
        group.setIsMandatory(request.getIsMandatory());
        group.setMinSelect(request.getMinSelect());
        group.setMaxSelect(request.getMaxSelect());
        return toAddonGroupDto(addonGroupRepository.save(group));
    }

    @Transactional
    public AddonItemDto addAddonItem(Long groupId, AddonItemDto request) {
        AddonGroup group = addonGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AddonGroup", "id", groupId));
        AddonItem item = new AddonItem();
        item.setAddonGroup(group);
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        item.setIsAvailable(true);
        return toAddonItemDto(addonItemRepository.save(item));
    }

    @Transactional
    public void linkAddonGroupToItem(Long itemId, Long addonGroupId) {
        if (addonMapRepository.existsByItemIdAndAddonGroupId(
                itemId, addonGroupId)) {
            throw new BusinessException(
                    "Addon group already linked to this item",
                    "ADDON_ALREADY_LINKED");
        }
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MenuItem", "id", itemId));
        AddonGroup group = addonGroupRepository.findById(addonGroupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AddonGroup", "id", addonGroupId));

        ItemAddonGroupMap map = new ItemAddonGroupMap();
        map.setItem(item);
        map.setAddonGroup(group);
        addonMapRepository.save(map);

        item.setHasAddons(true);
        itemRepository.save(item);
        log.info("Addon group {} linked to item {}", addonGroupId, itemId);
    }

    // ─── MAPPERS ─────────────────────────────────────

    private TaxGroupDto toTaxGroupDto(TaxGroup tg) {
        return TaxGroupDto.builder()
                .id(tg.getId())
                .branchId(tg.getBranch().getId())
                .name(tg.getName())
                .cgstPct(tg.getCgstPct())
                .sgstPct(tg.getSgstPct())
                .igstPct(tg.getIgstPct())
                .cessPct(tg.getCessPct())
                .isInclusive(tg.getIsInclusive())
                .isActive(tg.getIsActive())
                .build();
    }

    private CategoryDto toCategoryDto(MenuCategory c) {
        return CategoryDto.builder()
                .id(c.getId())
                .branchId(c.getBranch().getId())
                .parentCategoryId(c.getParentCategory() != null ?
                        c.getParentCategory().getId() : null)
                .parentCategoryName(c.getParentCategory() != null ?
                        c.getParentCategory().getName() : null)
                .name(c.getName())
                .displayOrder(c.getDisplayOrder())
                .colorCode(c.getColorCode())
                .isActive(c.getIsActive())
                .build();
    }

    private MenuItemDto toMenuItemDto(MenuItem item) {
        return MenuItemDto.builder()
                .id(item.getId())
                .branchId(item.getBranch().getId())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .taxGroupId(item.getTaxGroup() != null ?
                        item.getTaxGroup().getId() : null)
                .taxGroupName(item.getTaxGroup() != null ?
                        item.getTaxGroup().getName() : null)
                .name(item.getName())
                .shortName(item.getShortName())
                .description(item.getDescription())
                .foodType(item.getFoodType())
                .basePrice(item.getBasePrice())
                .itemCode(item.getItemCode())
                .hasVariants(item.getHasVariants())
                .hasAddons(item.getHasAddons())
                .isRecommended(item.getIsRecommended())
                .isBestseller(item.getIsBestseller())
                .isSeasonal(item.getIsSeasonal())
                .availableFromTime(item.getAvailableFromTime())
                .availableToTime(item.getAvailableToTime())
                .isAvailable(item.getIsAvailable())
                .isActive(item.getIsActive())
                .displayOrder(item.getDisplayOrder())
                .primaryImageUrl(
                        imageRepository.findByItemIdAndIsPrimaryTrue(item.getId())
                                .map(MenuItemImage::getImageUrl)
                                .orElse(null))
                .build();
    }

    @Transactional(readOnly = true)
    public MenuItemDto toMenuItemDtoWithDetails(MenuItem item) {
        MenuItemDto dto = toMenuItemDto(item);

        // load variants
        List<ItemVariantDto> variants = variantRepository
                .findByItemIdOrderByIsDefaultDesc(item.getId())
                .stream().map(this::toVariantDto)
                .collect(Collectors.toList());
        dto.setVariants(variants);

        // load addon groups with items
        List<AddonGroupDto> addonGroups = addonMapRepository
                .findByItemId(item.getId())
                .stream()
                .map(map -> {
                    AddonGroupDto groupDto = toAddonGroupDto(map.getAddonGroup());
                    return groupDto;
                })
                .collect(Collectors.toList());
        dto.setAddonGroups(addonGroups);

        return dto;
    }

    private ItemVariantDto toVariantDto(ItemVariant v) {
        return ItemVariantDto.builder()
                .id(v.getId())
                .itemId(v.getItem().getId())
                .variantName(v.getVariantName())
                .price(v.getPrice())
                .isDefault(v.getIsDefault())
                .isAvailable(v.getIsAvailable())
                .build();
    }

    private AddonGroupDto toAddonGroupDto(AddonGroup g) {
        List<AddonItemDto> items = addonItemRepository
                .findByAddonGroupId(g.getId())
                .stream().map(this::toAddonItemDto)
                .collect(Collectors.toList());
        return AddonGroupDto.builder()
                .id(g.getId())
                .branchId(g.getBranch().getId())
                .name(g.getName())
                .isMandatory(g.getIsMandatory())
                .minSelect(g.getMinSelect())
                .maxSelect(g.getMaxSelect())
                .items(items)
                .build();
    }

    private AddonItemDto toAddonItemDto(AddonItem i) {
        return AddonItemDto.builder()
                .id(i.getId())
                .addonGroupId(i.getAddonGroup().getId())
                .name(i.getName())
                .price(i.getPrice())
                .isAvailable(i.getIsAvailable())
                .build();
    }

    @Transactional
    public MenuItemImageDto uploadItemImage(Long itemId, MultipartFile file, Boolean isPrimary) {
        MenuItem item = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));
        try {
            java.nio.file.Path uploadDir = java.nio.file.Paths.get(menuImagePath);
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(
                            originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = "item_" + itemId + "_" +
                    System.currentTimeMillis() + extension;

            java.nio.file.Path filePath = uploadDir.resolve(filename);
            java.nio.file.Files.copy(file.getInputStream(), filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // If setting as primary — unset existing primary
            if (Boolean.TRUE.equals(isPrimary)) {
                imageRepository.findByItemIdAndIsPrimaryTrue(itemId)
                        .ifPresent(img -> {
                            img.setIsPrimary(false);
                            imageRepository.save(img);
                        });
            }

            // Get next display order
            List<MenuItemImage> existing = imageRepository.findByItemIdOrderByDisplayOrderAsc(itemId);
            int nextOrder = existing.size();

            MenuItemImage image = new MenuItemImage();
            image.setItem(item);
            image.setImageUrl("uploads/menu/" + filename);
            image.setIsPrimary(isPrimary != null ? isPrimary : existing.isEmpty());
            image.setDisplayOrder(nextOrder);

            MenuItemImage saved = imageRepository.save(image);
            log.info("Image uploaded for item {}: {}", itemId, filename);
            return toImageDto(saved);

        } catch (Exception e) {
            log.error("Image upload failed: {}", e.getMessage());
            throw new BusinessException(
                    "Image upload failed", "IMAGE_UPLOAD_FAILED");
        }
    }

    // [NEW] Get images ────────────────────────────
    @Transactional(readOnly = true)
    public List<MenuItemImageDto> getItemImages(Long itemId) {
        return imageRepository
                .findByItemIdOrderByDisplayOrderAsc(itemId)
                .stream().map(this::toImageDto)
                .collect(Collectors.toList());
    }

    // [NEW] Delete image ──────────────────────────
    @Transactional
    public void deleteItemImage(Long imageId) {
        MenuItemImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MenuItemImage", "id", imageId));
        imageRepository.delete(image);
        log.info("Image deleted: {}", imageId);
    }

    // [NEW] Mapper ────────────────────────────────
    private MenuItemImageDto toImageDto(MenuItemImage img) {
        return MenuItemImageDto.builder()
                .id(img.getId())
                .itemId(img.getItem().getId())
                .imageUrl(img.getImageUrl())
                .displayOrder(img.getDisplayOrder())
                .isPrimary(img.getIsPrimary())
                .build();
    }
}
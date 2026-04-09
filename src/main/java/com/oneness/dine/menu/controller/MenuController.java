package com.oneness.dine.menu.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.menu.dto.*;
import com.oneness.dine.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // ─── TAX GROUPS ──────────────────────────────────

    @GetMapping("/tax-groups")
    public ResponseEntity<ApiResponse<List<TaxGroupDto>>> getTaxGroups(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(menuService.getAllTaxGroups(branchId)));
    }

    @PostMapping("/tax-groups")
    public ResponseEntity<ApiResponse<TaxGroupDto>> createTaxGroup(
            @Valid @RequestBody TaxGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        menuService.createTaxGroup(request),
                        "Tax group created successfully"));
    }

    @DeleteMapping("/tax-groups/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTaxGroup(
            @PathVariable Long id) {
        menuService.deleteTaxGroup(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Tax group deleted successfully"));
    }

    // ─── CATEGORIES ──────────────────────────────────

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(menuService.getAllCategories(branchId)));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        menuService.createCategory(request),
                        "Category created successfully"));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        menuService.updateCategory(id, request),
                        "Category updated successfully"));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id) {
        menuService.deleteCategory(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Category deleted successfully"));
    }

    // ─── MENU ITEMS ───────────────────────────────────

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<MenuItemDto>>> getItems(
            @RequestParam Long branchId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    menuService.searchItems(branchId, search)));
        }
        if (categoryId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    menuService.getItemsByCategory(categoryId)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                menuService.getAllItems(branchId)));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ApiResponse<MenuItemDto>> getItemById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(menuService.getItemById(id)));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<MenuItemDto>> createItem(
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        menuService.createItem(request),
                        "Menu item created successfully"));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<MenuItemDto>> updateItem(
            @PathVariable Long id,
            @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        menuService.updateItem(id, request),
                        "Menu item updated successfully"));
    }

    @PatchMapping("/items/{id}/availability")
    public ResponseEntity<ApiResponse<MenuItemDto>> toggleAvailability(
            @PathVariable Long id,
            @Valid @RequestBody ItemAvailabilityRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        menuService.toggleAvailability(id, request),
                        "Item availability updated"));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long id) {
        menuService.deleteItem(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Menu item deleted successfully"));
    }

    // ─── VARIANTS ────────────────────────────────────

    @GetMapping("/items/{id}/variants")
    public ResponseEntity<ApiResponse<List<ItemVariantDto>>> getVariants(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(menuService.getVariants(id)));
    }

    @PostMapping("/items/{id}/variants")
    public ResponseEntity<ApiResponse<ItemVariantDto>> addVariant(
            @PathVariable Long id,
            @Valid @RequestBody ItemVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        menuService.addVariant(id, request),
                        "Variant added successfully"));
    }

    @DeleteMapping("/items/variants/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable Long variantId) {
        menuService.deleteVariant(variantId);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Variant deleted successfully"));
    }

    // ─── ADDON GROUPS ────────────────────────────────

    @GetMapping("/addon-groups")
    public ResponseEntity<ApiResponse<List<AddonGroupDto>>> getAddonGroups(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        menuService.getAllAddonGroups(branchId)));
    }

    @PostMapping("/addon-groups")
    public ResponseEntity<ApiResponse<AddonGroupDto>> createAddonGroup(
            @Valid @RequestBody AddonGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        menuService.createAddonGroup(request),
                        "Addon group created successfully"));
    }

    @PostMapping("/addon-groups/{groupId}/items")
    public ResponseEntity<ApiResponse<AddonItemDto>> addAddonItem(
            @PathVariable Long groupId,
            @RequestBody AddonItemDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        menuService.addAddonItem(groupId, request),
                        "Addon item added successfully"));
    }

    @PostMapping("/items/{itemId}/addon-groups/{addonGroupId}")
    public ResponseEntity<ApiResponse<Void>> linkAddonGroup(
            @PathVariable Long itemId,
            @PathVariable Long addonGroupId) {
        menuService.linkAddonGroupToItem(itemId, addonGroupId);
        return ResponseEntity.ok(
                ApiResponse.success(null,
                        "Addon group linked to item successfully"));
    }

    @PostMapping("/items/{id}/images")
    public ResponseEntity<ApiResponse<MenuItemImageDto>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "false")
            Boolean isPrimary) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        menuService.uploadItemImage(id, file, isPrimary),
                        "Image uploaded successfully"));
    }

    @GetMapping("/items/{id}/images")
    public ResponseEntity<ApiResponse<List<MenuItemImageDto>>> getImages(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(menuService.getItemImages(id)));
    }

    @DeleteMapping("/items/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long imageId) {
        menuService.deleteItemImage(imageId);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Image deleted successfully"));
    }

}
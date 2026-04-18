package com.oneness.dine.inventory.controller;

import com.oneness.dine.core.dto.ApiResponse;
import com.oneness.dine.inventory.dto.*;
import com.oneness.dine.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ─── INVENTORY ITEMS ─────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryItemDto>>> getAllItems(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.getAllItems(branchId)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryItemDto>>> getLowStock(
            @RequestParam Long branchId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.getLowStockItems(branchId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItemDto>> getItem(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(inventoryService.getItemById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryItemDto>> createItem(
            @Valid @RequestBody InventoryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        inventoryService.createItem(request),
                        "Inventory item created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItemDto>> updateItem(
            @PathVariable Long id,
            @RequestBody InventoryItemRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.updateItem(id, request),
                        "Inventory item updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.ok(
                ApiResponse.success(null,
                        "Inventory item deleted successfully"));
    }

    // ─── STOCK TRANSACTIONS ──────────────────────────

    @PostMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<StockTransactionDto>> addTransaction(
            @PathVariable Long id,
            @Valid @RequestBody StockTransactionRequest request) {
        request.setInventoryItemId(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        inventoryService.addTransaction(request),
                        "Stock transaction recorded successfully"));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<List<StockTransactionDto>>> getTransactions(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.getTransactions(id)));
    }

    // ─── RECIPE MANAGEMENT ───────────────────────────

    @GetMapping("/menu-items/{itemId}/ingredients")
    public ResponseEntity<ApiResponse<List<RecipeIngredientDto>>> getRecipe(
            @PathVariable Long itemId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.getRecipe(itemId)));
    }

    @PostMapping("/menu-items/{itemId}/ingredients")
    public ResponseEntity<ApiResponse<List<RecipeIngredientDto>>> saveRecipe(
            @PathVariable Long itemId,
            @Valid @RequestBody List<RecipeIngredientRequest> ingredients) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        inventoryService.saveRecipe(itemId, ingredients),
                        "Recipe saved successfully"));
    }
}
